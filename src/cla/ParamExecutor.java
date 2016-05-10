package cla;

import java.util.List;

/**
 * Modified 'Command pattern' for CLA parameters
 */
public interface ParamExecutor
{
	/**
	 * Execute code for param and values
	 * @param param parent parameter
	 * @param values parent param values
	 */
	void execute( Parameter param, List<String> values );
}
