# Interpretable Control Competition
This repository contains a solution for the [Interpretable Control Competition of the GECCO conference in 2024](https://gecco-2024.sigevo.org/HomePage).

## Repository Content
The repository contains:
1. `/simulation/...` Python game code
   1. `continuous/` The Walker2D game code
   2. `discrete/` The 2048 game code
   3. `evoal/` The EvoAl-specific controller and game state queries
   4. `generated/` The selected policy. Please note, that this path must be deleted to run the optimisation process
2. `/evoal-configuration/...` The pipeline configuration
   1. `2048.ddl` The data definitions for EvoAl
   2. `model.dl` The policy model used during optimisation
   3. `search.ol` Configuration of the EA.
3. `/evoal-plugin/` The EvoAl plugin that implements the custom fitness function.
4. `/evoal-release/` The latest EvoAl release (including the developed extension).
5. `/winning-run/...` Output of the winning run
   1. `protocols-of-evolution-run.zip` The complete protocol (including all policies and outputs of the evolution run)
   2. `protocol-of-best/policies` The Python policy and protocols
   3. `protocol-of-best/search` fitness metrics collects during the run, such as the policy score.
6. `/01-run-search.sh` The run script. Simply execute `$SH 01-run-search.sh`.
7. `contribution.pdf` Description of the pipeline.

## Challenge Home Page
You find the original challenge code in the [challenge repository](https://github.com/giorgia-nadizar/interpretable-control-competition).

## Running the winning policy
1. Clone the repository
2. Follow the installation instructions from the [challenge repository](https://github.com/giorgia-nadizar/interpretable-control-competition)
3. Go into `simulation` and execute `PYTHONPATH=. python3 evoal/controller.py`

## Running the optimisation process
1. Clone the repository
2. Follow the installation instructions from the [challenge repository](https://github.com/giorgia-nadizar/interpretable-control-competition)
3. Execute `$SH 01-run-search.sh`
4. The generated policies will be generated into your temporary folder. You will find in $TMPDIR/ic-program-&lt;program-id>-&lt;random-value>/

## Configuration
The configuration can be found in the repository in the folder `evoal-configuration`. The [model configuration](evoal-configuration/model.dl) describes the policy model we used. The evolutionary algorithm configuration is present (and documented) in the [search.ol](evoal-configuration/search.ol).

## The winning policy
Since we executed the policy six times each, we will state the minimum, average, and maximum of the fitness values we measured.
|         | Total Score | Move Count | Highest Tile |
| ------- | -----------:|  ---------:| ------------:|
| Minimum |        9292 |        625 |          512 |
| Average |       14093 |        816 |         1276 |
| Maximum |       21280 |       1046 |         2048 |

The following code is the Python implementation of the winning policy:
```Python
import numpy as np
import evoal.game as game

def control(observation: np.ndarray) -> int:
  """Generated policy function for the game 2048"""

  if game.scoreGains(observation, direction1 = 2, direction2 = 3) > game.scoreGains(observation, direction1 = 1, direction2 = 0) \
     or game.scoreGains(observation, direction1 = 2, direction2 = 2) > game.scoreGains(observation, direction1 = 3, direction2 = 2):
    return game.toAction(2)
  if game.scoreGains(observation, direction1 = 1, direction2 = 0) < game.scoreGains(observation, direction1 = 3, direction2 = 2) \
     or not game.canMoveInDirection(observation, direction = 1):
    return game.toAction(3)
  if game.scoreGains(observation, direction1 = 2, direction2 = 3) > game.scoreGains(observation, direction1 = 1, direction2 = 1) \
     or game.scoreGains(observation, direction1 = 2, direction2 = 1) > game.scoreGains(observation, direction1 = 0, direction2 = 1):
    return game.toAction(2)

  return game.toAction(1)
```

The winning policy focuses on increasing the score gain and chooses to go in the direction that promises higher score gain. The remaining queries, such as *willBeSorted*, are part of some policies but did not make it into the best policy. At the same time, the policy only uses three out of four directions, which might leave room for further improvement, but we assume that the situation where the board would have to be moved into the fourth direction occurs very seldom. Having a given board situation, the policy allows one to explain precisely why a certain move was made. On the one hand, the state queries are easy to understand and, on the other hand, they can be calculated for a given board to show the decision process.

