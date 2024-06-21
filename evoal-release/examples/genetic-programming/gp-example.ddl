import "definitions" from de.evoal.core.ea.'genetic-programming';

module 'gp-example' {
  data:
    /**
     * Quality measure of the generated function.
     */
    quotient real data 'ℝ';
    
    /**
	 * The metric function to search.
	 */
	data 'regression-function' of instance 'program';


    //////////////////////////////////////////////////////////////////////////
    // metrics for the program generation
    quotient real data 'x0';
    quotient real data 'y0';

}