package cla;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Map to store parsed parameters and their values 
 */
public class ParsedParameterMap
{
	//////////
	// FIELDS
	//////////
	
	private Map<String,List<String>> parameterValues = new HashMap<>();

	
	///////////
	// METHODS
	///////////
	
	/**
	 * Get parsed parameter names
	 * @return an array with the parsed parameter names
	 */
	public String[] getParameterNames()
	{ 
		return parameterValues.keySet().stream().toArray( String[]::new );
	}
	
	/**
	 * Get values for a parameter
	 * @param name the name of the parameter
	 * @return an array with the parameter values or null if the parameter is not in map
	 */
	public String[] getValues( String name )
	{
		List<String> values = parameterValues.get( name ); 
		
		return values != null ? values.stream().toArray( String[]::new ) : null;
	}

	/**
	 * Add a value to a parameter. Use null as value to add only the parameter
	 * @param name the parameter name
	 * @param value the parameter value
	 * @return the parsed paramter map itself
	 */
	public ParsedParameterMap addValue( String name, String value )
	{
		if ( parameterValues.containsKey( name ) )
		{
			if ( value != null )
				parameterValues.get( name ).add( value );
		}
		else
		{
			List<String> values = new ArrayList<>();
			if ( value != null )
				values.add( value );
			parameterValues.put( name, values );
		}
		
		return this;
	}
	
	/**
	 * Check for parameter in map
	 * @param param the parameter name
	 * @return true if the parameter is in map
	 */
	public boolean containsParam( String param ) 
	{ 
		return parameterValues.containsKey( param );
	}
	
	@Override
	public String toString()
	{
		List<String> params = new ArrayList<>();
		for ( Entry<String,List<String>> entry : parameterValues.entrySet() )
			params.add( entry.getKey() + " " + Arrays.toString( 
										entry.getValue().stream().toArray() ) );
		
		return "[Parsed input: " + String.join( ",", params ) + "]";
	}
}
