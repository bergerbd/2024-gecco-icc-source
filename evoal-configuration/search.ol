import "definitions" from de.evoal.core.math;

import "definitions" from de.evoal.core.optimisation;
import "definitions" from de.evoal.core.ea.optimisation;
import "definitions" from de.evoal.core.ea.mdo;

import "definitions" from de.evoal.challenges.ic.optimisation;

import "data" from '2048';

module search {
	specify problem 'game-2048' {
		description := "Searching a policy for solving the game 2048.";
		
		/**
		 * We are searching for policy
		 */
		'search-space' := [
            data 'policy'
		];
		
		/**
		 * The optimisation space contains several information on the solution for a better manual
		 *   inspection of the results.
		 * 
		 * Please note: We do not use all values in the fitness function. The relevant data is
		 *   mentioned in the comparator configuration.
		 */
		'optimisation-space' := [data 'average(total-score)',
								 data 'average(move-count)',
								 data 'min(highest-tile)',
								 data 'average(highest-tile)',
								 data 'max(highest-tile)',
								 data 'program-id',
								 data 'policy-size'];

		'maximise' := true;

		'optimisation-function' := 'unknown-function' {};
	}
		

	/**
	 * We use an evolutionary algorithm for the solution.
	 */		
	configure 'evolutionary-algorithm' for 'game-2048' {
		/**
		 * Configure same basic information.
		 */
		'number-of-generations' := 333;
		'size-of-population' := 100;
		'maximum-age' := 333;
		'offspring-fraction' := 0.7;
	
		/**
		 * The initial program candidates are randomly generated.
		 */
	    'initialisation' := 'random-model-population' {};

		/**
		 * We use the following data entries for comparison of individuals. The
		 *   comparator compares the first data entry and returns the result, as
		 *   long as the values aren't equal. Otherwise, the next data entry is
		 *   compared.
		 */	    	
		'comparator' := 'hierarchical-comparator' {
			order := [
				data 'average(highest-tile)',
				data 'max(highest-tile)',
				data 'min(highest-tile)',
				data 'average(total-score)'
			];
		};
		
		/**
		 * The genotype configuration is straight forward, as it simply maps the
		 *   policy to a model-based chromosome.
		 */
	    genotype := 'model-genotype' {
	        chromosomes := [
	            'model-chromosome' {
	              root   := data 'policy';
	            }
	        ];   
	    };

		/**
		 * No handlers needed.
		 */	
	    handlers := [];
	
		/**
		 * Survivor and offspring selection configuration
		 */
	    selectors := selectors {
	        survivor := 'elite-selector' {
                'size-factor' := 1.0;
                'non-elite-selector' := 'tournament-selector' {
                    'size-factor' := 0.05	;
                };
	        };

	        offspring := 'elite-selector' {
                'size-factor' := 1.0;
                'non-elite-selector' := 'tournament-selector' {
                    'size-factor' := 0.05	;
                };
            };            
	    };
	    
	    /**
	     * Alterers to use.
	     */
		alterers := alterers {
	        crossover := [
	        	/**
	        	 * Randomly swaps compatible elements of two individuals.
	        	 */
	        	'feature-swap-crossover' {
	        		probability := 0.4;
	        	}
	        ];
	        mutator := [
	        	/**
	        	 * The single choice mutator makes sure that only one of its
	        	 *   child mutators is employed. Otherwise, we mutate an individual
	        	 *   very strongly in a single generation.
	        	 */
	        	'single-choice-mutator' {
	        		alterers := [
			        	/**
			        	 * Mutates literals, such as int and boolean
			        	 */
				        'literal-mutator' {
				        		probability := 0.3;
				        } ,

			        	/**
			        	 * Mutates literals, such as int and boolean
			        	 */
				        'plus-one-mutator' {
				        		probability := 0.3;
				        },
	
						/**
						 * Adds or removes elements from/to arrays.
						 */		        
				        'array-size-mutator' {
				        		probability := 0.3;
				        },
				        
				        /**
				         * Reorders array elements. This is interesting to reorder
				         *   and clauses to find good combinations.
				         */
				        'array-reorder-mutator' {
				        		probability := 0.3;
				        },
				        
				        /**
				         * Changes all attributes of a policy the same way. This will
						 *   rotate a given policy.
				         */
				        'policy-rotation-mutator' {
				        		'probability' := 0.3;
				        		attributes := ["action", "direction", "direction1", "direction2"];
				        }			        
		        ];
		    }	        
	        ];
	    };
	
		/**
		 * Use our custom fitness function and configure the game repetition amount.
		 */
		'optimisation-function' := '2048-fitness' {
			'game-count' := 6;
		};

		/**
		 * Log all candidates of every generation. Do not store the search space (the
		 *   program). Instead, we will use the `program-id` to have a reference to
		 *   the policy.py file on disk (policies/ic-program-`program-id`-.../generated/policy.py).
		 */		
		documenting := ['candidates-per-generation' {'store-search-space' := false; }];
	}
}