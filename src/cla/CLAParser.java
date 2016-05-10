package cla;

import static java.lang.String.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * <strong>{@value #LIBNAME}</strong><br>
 * @version {@value #VERSION}
 * @author {@value #AUTHOR}, {@value #COPYLEFT}
 */
public class CLAParser
{
	//////////
	// FIELDS
	//////////

	public static final String LIBNAME = "CLA (Command Line Args)";
	public static final String VERSION = "1.0";
	public static final String AUTHOR = "Miguel Alejandro Moreno Barrientos";
	public static final String COPYLEFT = "2016";
	public static final String LICENSE = "GPLv3";

	// (sequences and quotes)
	private static final String REGEX = "(\"[^\"]*\")|(\\S+)"; 	
	
	private Map<String,Parameter> parameterMap = new HashMap<>();

	
	////////////////
	// CONSTRUCTORS
	////////////////
	
	public CLAParser() {}
	

	///////////
	// METHODS
	///////////
	
	/**
	 * Adds a new parameter to parser
	 * @param param new parameter
	 * @return this - chaining method
	 */
	public CLAParser addParameter( Parameter param )
	{
		parameterMap.put( param.getName(), param );
		
		return this;
	}
	
	/**
	 * Return parameter from parameter name
	 * @param name parameter name (without delimiter)
	 * @return the parameter or null if doesn't exist
	 */
	public Parameter getParameter( String name )
	{
		return parameterMap.get( name );
	}
	
	/**
	 * Get all parameters defined in this parser
	 * @return an array with all parameters
	 */
	public Parameter[] getParameters()
	{
		return parameterMap.values().stream().toArray( Parameter[]::new );
	}

	/**
	 * Parse a command line parametrized input
	 * @param tokens params array
	 * @return map (key:parameter_name)/(value:param-values)
	 * @throws InputMismatchException
	 * @throws NoSuchElementException
	 */
	public ParsedParameterMap parse( String... tokens )
	{
		ParsedParameterMap parsedParameters = new ParsedParameterMap();

		// no parameters
		if ( tokens.length == 0 )
			return parsedParameters;
		
		// main loop
		int index = 0;
		while ( index < tokens.length )
		{
			// get current token and parameter
			String token = tokens[ index++ ];
			Optional<Parameter> optParam = getParameterFromToken( token );
			Parameter parameter = optParam.orElseThrow( 
				() -> new InputMismatchException( 
									format( "Unknown param \"%s\"", token ) ) );

			// add parameter to parsed parameter map
			if ( parsedParameters.containsParam( parameter.getName() ) )
				throw new InputMismatchException( 
					format( "Duplicate param \"%s\"", parameter.getName() ) );
			parsedParameters.addValue( parameter.getName(), null );
			
			// read values			

			// do nothing with parameter without extra values
			if ( parameter.getMaxValues() == 0 )  continue;
			
			// check enough values
			if ( tokens.length - index < parameter.getMinValues() )
				throw new NoSuchElementException( format( 
					"Not enough values for param \"%s\". Needs at least %s",
					parameter.getName(), parameter.getMinValues() ) );
			
			// read values for param until min values
			String value;
			int end = index + parameter.getMinValues();
			for ( ; index < end; index++ )   
			{
				value = tokens[ index ];
				if ( parameter.fixedValues() 
					 && !parameter.containsValue( value ) )
					throw new InputMismatchException( format( 
										"Value \"%s\" invalid for param \"%s\"", 
										value, parameter.getName() ) );
				if ( getParameterFromToken( value ).isPresent() )
					System.out.println( format( 
						"Warning: value \"%s\" for parameter \"%s\" "
						+ "equals to parameter \"%s\". Maybe an error?", 
						value, parameter.getName(),
						getParameterFromToken( value ).get().getName() ) );
				parsedParameters.addValue( parameter.getName(), value );
			}
			
			// read additional values
			while ( index < tokens.length 
					&& parsedParameters.getValues( parameter.getName() ).length 
					   < parameter.getMaxValues()
					&& !( optParam = getParameterFromToken( 
									value = tokens[ index ] ) ).isPresent() )
			{
				index++;
				if ( parameter.fixedValues() 
					 && !parameter.containsValue( value ) )
					throw new InputMismatchException( format( 
									"Value \"%s\" invalid for parameter \"%s\"", 
									value, parameter.getName() ) );
				parsedParameters.addValue( parameter.getName(), value );
			}
		}
		
		// check for required params
		for ( String param : parameterMap.keySet() )
			if ( parameterMap.get( param ).isRequired() 
				 && parsedParameters.getValues( param ) == null )
				throw new InputMismatchException( 
								format( "Parameter \"%s\" required", param ) );		
		
		return parsedParameters;
	}
	
	/**
	 * Parse a command line parametrized input
	 * @param input command line input
	 * @return map (key:parameter_name)/(value:param-values)
	 * @throws InputMismatchException
	 * @throws NoSuchElementException
	 */
	public ParsedParameterMap parse( String input )
	{
		return parse( compileRegex( input ) );
	}	
 	
	/**
	 * Execute all param executors defined in the parsed parameter map sorted
	 * by executor index (from lowest to highest)
	 * @param parsedParameters parsed param-values map
	 */
	public void execute( ParsedParameterMap parsedParameters )
	{
		Stream.of( parsedParameters.getParameterNames() )
		.map( parameterMap::get )
		.filter( Objects::nonNull )
		.filter( p -> p.getExecutor() != null )
		.sorted( (p1,p2) -> Integer.compare( 
							p1.getExecutorIndex(), p2.getExecutorIndex() ) )
		.forEach( param -> param.getExecutor().execute( 
			param, 
			Arrays.asList( parsedParameters.getValues( param.getName() ) ) ) ); 
	}

	/**
	 * Parse and execute in one step
	 * @param input command line input
	 * @return map (key:parameter_name)/(value:param-values)
	 */
	public ParsedParameterMap parseAndExecute( String input )
	{
		return parseAndExecute( compileRegex( input ) );
	}
	
	/**
	 * Parse and execute in one step
	 * @param tokens params array
	 * @return map (key:parameter_name)/(value:param-values)
	 */
	public ParsedParameterMap parseAndExecute( String... tokens )
	{
		ParsedParameterMap parsedParameters = parse( tokens );
		execute( parsedParameters );
		
		return parsedParameters;
	}
	
	/**
	 * Get parameter 'token' in parameter map if exists
	 * @param token input token
	 * @return parameter if exists
	 */
	public Optional<Parameter> getParameterFromToken( String token )
	{
		return parameterMap.values()
			   .stream()
			   .filter( p -> p.getCompleteName().equals( token ) )
			   .findFirst();
	}
	
	/**
	 * Add a default help parameter to print command info in a PrintStream
	 * @param out print buffer (like System.out)
	 * @param description a help description, shown on top
	 * @param name parameter name (tipically help or h or ?)
	 * @param prefix param prefix
	 * @return the help default parameter added to parser
	 */
	public Parameter addDefaultHelpParameter( PrintStream out, 
							String description, String name, String prefix )
	{
		ParamExecutor executor = (param,values) -> {
			out.println( "==================================================" );
			out.println( "Help:" );
			out.println( description );
			out.println( "parameter [values{cardinality}]   \"description\"" );
			out.println( "__________________________________________________" );
			Stream.of( getParameters() )
			.sorted( 
				(p1,p2) -> p1.getName().compareToIgnoreCase( p2.getName() ) )
			.forEach( out::println );
			out.println( "==================================================" );
		};
		
		Parameter help = new Parameter( name, prefix, "", 
						"Help about this command", false, 0, 0, null, executor, 
						Integer.MIN_VALUE );
		addParameter( help );
		
		return help;
	}
	
	/**
	 * Split a input string using regex
	 * @param input the input string 
	 * @return an split array from input
	 */
	private String[] compileRegex( String input )
	{
		Matcher match = Pattern.compile( REGEX ).matcher( input );
		List<String> tokens = new ArrayList<>();
		while ( match.find() )
				tokens.add( match.group( 1 ) != null 
				? match.group( 1 ).substring( 1, match.group( 1 ).length() - 1 )
								  // (remove quotes)
				: match.group( 2 ) );

		return tokens.stream().toArray( String[]::new );
	}
}
