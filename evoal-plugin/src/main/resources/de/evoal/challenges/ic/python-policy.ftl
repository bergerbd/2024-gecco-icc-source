import numpy as np
import evoal.game as game

def control(observation: np.ndarray) -> int:
  """Generated policy function for the game 2048"""

<#assign policy = model["policy"]>
<#list access.entries(policy) as entry>
  <#assign or = access.condition(entry)>
  if ${access.subexpressions(or)?map(and -> access.comparisons(and)?map(cmp -> access.toExpression(cmp))?join(" and "))?join(" or ")}:
    return game.toAction(${access.action(entry)})
</#list>

  return game.toAction(${access.action(policy)})