import numpy as np
import evoal.game as game


def control(observation: np.ndarray) -> int:
  """Generated policy function for the game 2048"""

  if game.scoreGains(observation, direction1 = 2, direction2 = 3) > game.scoreGains(observation, direction1 = 1, direction2 = 0) or game.scoreGains(observation, direction1 = 2, direction2 = 2) > game.scoreGains(observation, direction1 = 3, direction2 = 2):
    return game.toAction(2)
  if game.scoreGains(observation, direction1 = 1, direction2 = 0) < game.scoreGains(observation, direction1 = 3, direction2 = 2) or not game.canMoveInDirection(observation, direction = 1):
    return game.toAction(3)
  if game.scoreGains(observation, direction1 = 2, direction2 = 3) > game.scoreGains(observation, direction1 = 1, direction2 = 1) or game.scoreGains(observation, direction1 = 2, direction2 = 1) > game.scoreGains(observation, direction1 = 0, direction2 = 1):
    return game.toAction(2)

  return game.toAction(1)