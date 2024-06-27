import numpy as np
from discrete.env_2048.grid2048 import Grid as Grid

def toAction(direction : int) -> int:
    if direction == 0:
        return 0
    elif direction == 1:
        return 3
    elif direction == 2:
        return 1
    else:
        return 2  

def toInput(direction : int) -> str:
    if direction == 0:
        return 'W'
    elif direction == 1:
        return 'D'
    elif direction == 2:
        return 'S'
    else:
        return 'A'
 
def canMoveInDirection(observation : np.ndarray, direction : int) -> bool:
    ''' Checks if it is possible to move into the given direction. '''
    grid = Grid.create_from_array(observation)
    input = toInput(direction)

    return grid.is_valid_move(input)

def canMoveInDirections(observation : np.ndarray, direction1 : int, direction2 : int) -> bool:
    ''' Checks if it is possible to move into the given direction. '''
    grid = Grid.create_from_array(observation)

    if not grid.is_valid_move(toInput(direction1)):
        return False

    _, grid = grid.move(toInput(direction1))

    return grid.is_valid_move(toInput(direction2))
    
def scoreGain(observation : np.ndarray, direction : int) -> int:
    grid = Grid.create_from_array(observation)

    return grid.move(toInput(direction))[0]

def scoreGains(observation : np.ndarray, direction1 : int, direction2 : int) -> int:
    grid = Grid.create_from_array(observation)

    if not grid.is_valid_move(toInput(direction1)):
        return 0

    score1, grid = grid.move(toInput(direction1))

    if not grid.is_valid_move(toInput(direction2)):
        return score1

    score2, grid = grid.move(toInput(direction2))

    return score1 + score2

def isSorted(data : list) -> bool:
    data = list(data)
    while 0 in data:
        data.remove(0)

    if len(data) < 3:
        return True
    elif len(data) == 3:
        return (data[0] <= data[1] and data[1] <= data[2]) or (data[0] >= data[1] and data[1] >= data[2])
    else:
        return (data[0] <= data[1] and data[1] <= data[2] and data[2] <= data[3]) or (data[0] >= data[1] and data[1] >= data[2] and data[2] >= data[3])

def willBeUnsorted(observation : np.ndarray, direction : int) -> bool:
    '''
        Checks if a move into the given direction will make the board more "unsorted" then
        it is at the moment. A board is unsorted if one of its rows or columns is not sorted
        either increasingly or decreasingly. 
    '''
    observation = np.rot90(observation, direction)

    presorted0 = isSorted(observation[0])
    presorted1 = isSorted(observation[1])
    presorted2 = isSorted(observation[2])
    presorted3 = isSorted(observation[3])

    grid = Grid.create_from_array(observation)
    _, grid = grid.move('W')
    observation = grid.array()

    postsorted0 = isSorted(observation[0])
    postsorted1 = isSorted(observation[1])
    postsorted2 = isSorted(observation[2])
    postsorted3 = isSorted(observation[3])

    return sum((presorted0, presorted1, presorted2, presorted3)) > \
            sum((postsorted0, postsorted1, postsorted2, postsorted3))

def willBeSorted(observation : np.ndarray, direction : int) -> bool:
    '''
        Checks if a move into the given direction will make the board more "sorted" then
        it is at the moment. A board is unsorted if one of its rows or columns is not sorted
        either increasingly or decreasingly. 
    '''
    observation = np.rot90(observation, direction)

    presorted0 = isSorted(observation[0])
    presorted1 = isSorted(observation[1])
    presorted2 = isSorted(observation[2])
    presorted3 = isSorted(observation[3])

    grid = Grid.create_from_array(observation)
    _, grid = grid.move('W')
    observation = grid.array()

    postsorted0 = isSorted(observation[0])
    postsorted1 = isSorted(observation[1])
    postsorted2 = isSorted(observation[2])
    postsorted3 = isSorted(observation[3])
    
    return sum((presorted0, presorted1, presorted2, presorted3)) <= \
            sum((postsorted0, postsorted1, postsorted2, postsorted3))

def isLargestInCorner(observation : np.ndarray) -> bool:
    grid = Grid(observation)

    tile, _, _ = grid.highest_tile()

    return grid.get(0, 0) == tile or \
           grid.get(0, 3) == tile or \
           grid.get(3, 0) == tile or \
           grid.get(3, 3) == tile

def willLargestBeInCorner(observation : np.ndarray, direction : int) -> bool:
    grid = Grid(observation)

    if not grid.is_valid_move(toInput(direction)):
        return isLargestInCorner(observation)
    
    _, grid = grid.move(toInput(direction))
    return isLargestInCorner(grid.array())

def willLargestBeAtSide(observation : np.ndarray, direction : int) -> bool:
    grid = Grid(observation)

    if not grid.is_valid_move(toInput(direction)):
        return isLargestAtSide(observation)
    
    _, grid = grid.move(toInput(direction))
    return isLargestAtSide(grid.array())

def isLargestAtSide(observation : np.ndarray) -> bool:
    grid = Grid(observation)

    tile, _, _ = grid.highest_tile()

    return grid.get(0, 0) == tile or \
           grid.get(0, 1) == tile or \
           grid.get(0, 2) == tile or \
           grid.get(0, 3) == tile or \
           grid.get(1, 3) == tile or \
           grid.get(2, 3) == tile or \
           grid.get(3, 3) == tile or \
           grid.get(3, 2) == tile or \
           grid.get(3, 1) == tile or \
           grid.get(3, 0) == tile or \
           grid.get(2, 0) == tile or \
           grid.get(1, 0) == tile