package cla;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * CLA parameter class
 */
public class Parameter
{
	//////////
	// FIELDS
	//////////
	
	private String name;
	private String description;
	private int minValues = 0;
	private int maxValues = 0;
	private String prefix = "-";
	private String sufix = "";
	private Set<String> valueSet = new HashSet<>();
	private boolean required = false; 
	private ParamExecutor executor = null;
	private int executorIndex = 0;
	
	
	////////////////
	// CONSTRUCTORS
	////////////////
	
	/**
	 * Create a new CLA parameter with default values;
	 * optional, no values, prefix '-', empty sufix, no executor
	 * @param name name of the parameter (without delimiter)
	 * @param description description of the parameter
	 */
	public Parameter( String name, String description )
	{
		this.name = name;
		this.description = description;
	}

	/**
	 * Create a new CLA parameter without executor
	 * @param name name of the parameter (without delimiter)
	 * @param prefix prefix of the parameter
	 * @param sufix sufix of the parameter
	 * @param description description of the parameter
	 * @param required optional or required
	 * @param minValues minimum number of values
	 * @param maxValues maximum number of values
	 * @param values collection of fixed values for this param
	 */
	public Parameter( String name, String prefix, String sufix, 
		String description, boolean required, int minValues, int maxValues, 
		Collection<String> values )
	{
		this( name, description );
		this.prefix = prefix;
		this.sufix = sufix;
		this.required = required;
		this.minValues = minValues;
		this.maxValues = maxValues;
		if ( values != null )
			valueSet = new HashSet<>( values );
	}

	/**
	 * Create a new CLA parameter
	 * @param name name of the parameter (without delimiter)
	 * @param prefix prefix of the parameter
	 * @param sufix sufix of the parameter
	 * @param description description of the parameter
	 * @param required optional or required
	 * @param minValues minimum number of values
	 * @param maxValues maximum number of values
	 * @param values collection of fixed values for this param
	 * @param executor command to execute with this param or null
	 * @param executorIndex execution index, bigger value, later execution
	 */
	public Parameter( String name, String prefix, String sufix, 
		String description, boolean required, int minValues, int maxValues, 
		Collection<String> values, ParamExecutor executor, int executorIndex )
	{
		this( name, prefix, sufix, description, required, minValues, maxValues, 
			  values );
		this.executor = executor;
		this.executorIndex = executorIndex;
	}
	
	
	///////////////////
	// GETTERS/SETTERS
	///////////////////

	// name
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	// description
	public String getDescription() { return description; }
	public void setDescription(String description) { 
		this.description = description;
	}

	// minValues
	public int getMinValues()
	{ 
		return valueSet.isEmpty() 
			   ? minValues 
			   : Math.min( valueSet.size(), minValues );
	}	
	public void setMinValues( int minValues ) { this.minValues = minValues; }

	// maxValues
	public int getMaxValues()
	{ 
		return valueSet.isEmpty() 
			   ? maxValues 
			   : Math.min( valueSet.size(), maxValues );
	}	
	public void setMaxValues( int maxValues ) { this.maxValues = maxValues; }

	// prefix
	public String getPrefix() { return prefix; }
	public void setPrefix( String prefix ) { this.prefix = prefix; }

	// sufix
	public String getSufix() { return sufix; }
	public void setSufix( String sufix ) { this.prefix = sufix; }
	
	// required
	public boolean isRequired() { return required; }
	public void setRequired( boolean required ) { this.required = required; }
	
	// executor
	public ParamExecutor getExecutor() { return executor; }
	public void setExecutor( ParamExecutor executor )
	{ 
		this.executor = executor;
	}

	// executorIndex
	public int getExecutorIndex() { return executorIndex; }
	public void setExecutorIndex( int executorIndex )
	{
		this.executorIndex = executorIndex;
	}
	
	
	///////////
	// METHODS
	///////////
	
	/**
	 * Get name with delimiter (example: -help, --all)
	 * @return delimiter + name
	 */
	public String getCompleteName()
	{
		return getPrefix() + getName() + getSufix();
	}

	/**
	 * Add fixed value to param
	 * @param value fixed value
	 */
	public void addValue( String value )
	{
		valueSet.add( value );
	}
	
	/**
	 * Check if parameter contains value
	 * @param value value to check
	 * @return true if the value exists
	 */
	public boolean containsValue( String value )
	{
		return valueSet.contains( value );
	}
	
	/**
	 * Get number of values
	 * @return current number of values
	 */
	public int valuesSize() { return valueSet.size(); }	
	
	/**
	 * Check if parameter uses fixed or free values
	 * @return true for fixed values or false for free
	 */
	public boolean fixedValues() { return !valueSet.isEmpty(); }
	
	@Override
	public String toString()	
	{
		String cardinal = getMaxValues() == 0
			? ""
			: "{" + getMinValues()
			  + ( getMinValues() == getMaxValues()
			  	  ? ""
			  	  : "-" + ( getMaxValues() != Integer.MAX_VALUE 
			  	  		  ? getMaxValues() 
			  	  		  : "inf" ) )
			  + "}";
		String values = ( valueSet.isEmpty()
			? ( getMaxValues() > 0 ? " value" : "" )
			: " [" 
			  + String.join( "|", valueSet.stream().toArray( String[]::new ) )
			  + "]" ) 
			+ cardinal;
		String description = getDescription() != null 
							 && !getDescription().isEmpty()
							 ? "   \"" + getDescription() + "\""
							 : "";
		String required = isRequired() ? "   <<required>>" : ""; 
				
		return getCompleteName() + values + description + required;
	}
}
