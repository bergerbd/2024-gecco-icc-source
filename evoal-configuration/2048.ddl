import "definitions" from model;

/**
 * The data representation of the 2048 optimisation problem.
 */
module '2048' {
	data:
        /**
         * The policy to search. The structure of a policy is defined in model.dl.
         */
		data 'policy' of instance 'policy';

	
		/**
		 * The program's ID allows to map the result to its Python counter-part.
		 */
		quotient integer data 'program-id';
		
		/**
		 * Size of the policy in numbers of elements. Is an indirect measure of
		 *   the policy's complexity.
		 */
		quotient integer data 'policy-size';

		/**
		 * The average total score of all runs of a single policy.
		 */		
		quotient integer data 'average(total-score)';
		 
		/**
		 * The average move count of all runs of a single policy.
		 */		
		quotient integer data 'average(move-count)';
		 
		/**
		 * The minimum highest tile of all runs of a single policy.
		 */		
		quotient integer data 'min(highest-tile)';

		/**
		 * The average highest tile of all runs of a single policy.
		 */		
		quotient integer data 'average(highest-tile)';

		/**
		 * The maximum highest tile of all runs of a single policy.
		 */		
		quotient integer data 'max(highest-tile)';
}