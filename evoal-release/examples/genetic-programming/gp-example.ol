import "definitions" from de.evoal.core.math;

import "definitions" from de.evoal.core.optimisation;
import "definitions" from de.evoal.generator.optimisation;
import "definitions" from de.evoal.generator.generator;

import "definitions" from de.evoal.core.ea.optimisation;
import "definitions" from de.evoal.core.ea.'genetic-programming';

import "data" from 'gp-example';

module example {
  specify problem 'regression-function-search' {
    description := "Genetic Programming Example";
    'search-space' := [data 'regression-function'];
    'optimisation-space' := [data 'ℝ'];
    'maximise' := false;
    'optimisation-function' := 'unknown-function' {
    };
  }

  configure 'evolutionary-algorithm' for 'regression-function-search' {
    'number-of-generations' := 1000;
    'size-of-population' := 100;
    'maximum-age' := 1000;

    'initialisation' := 'random-tree-population' {};

    'comparator' := 'numeric-comparator' {};

    genotype := 'program-genotype' {
        chromosomes := [
            'program-chromosome' {
              content   := data 'regression-function';
              variables := [ data 'x0' ];
              operations := [ plus {}, multiply {}, minus {}, divide {}, sqrt {}, pow {}];
              constants := [ constant { name := "PI"; value := 'π'; } ];
              'ephemeral-constants' := [ 'ephemeral-constant' { lower := -50; upper := 50; count := 2; } ];
              'validators' := [ 'must-use-variable' { count := 1; }, 'program-size' { 'max-size' := 64; } ];

              'initial-depth' := 5;
            }
        ];
    };

    handlers := [];

    selectors := selectors {
        offspring := 'roulette-wheel-selector' {};
        survivor := 'elite-selector' {
                'size-factor' := 0.3;
                'non-elite-selector' := 'tournament-selector' {
                        'size-factor' := 0.1;
                };
        };
    };

    alterers := alterers {
      crossover := [
        'single-node-crossover' {
          probability := 0.3;
        }
      ];
      mutator := [
        'probability-mutator' {
          probability := 0.2;
        },
        'mathematical-expression-rewriter' {
          probability := 0.4;
        }
      ];      
    };

    'optimisation-function' := 'regression-fitness' {
      calculations := [
        'squared-error' {
          function := data 'regression-function';
          input := [ data 'x0' ];
          output := [ data 'y0' ];
          reference := "gp-example-function-data.csv";
    }];
    };

    documenting := [ 'best-candidate-per-generation' {} ];
  }
}