import "definitions" from de.evoal.surrogate.ml;
import "definitions" from de.evoal.surrogate.smile.ml;

import "data" from surrogate;

module training {
	prediction svr
		maps 'x:0'
	    to 'y:0'
	    using
	    	layer transfer
				with function 'gaussian-svr'
					mapping 'x:0'
					to 'y:0'
					with parameters
						'ε' := 1.4;
						'σ' := 3.0;
						'soft-margin' := 0.15;
						tolerance := 0.1;
	
	for _counter in [1 to 10] loop
	        predict svr from "data.json"
	        and measure
	                'cross-validation'(10);
	                'R²'();
	        end
	        and store to "svr_${_counter}.pson"
	end
}