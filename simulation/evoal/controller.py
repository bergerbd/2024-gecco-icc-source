import gymnasium as gym
from discrete.discrete_controller import DiscreteController
import numpy as np
import sys
import generated.policy as policy

from typing import Optional

class Controller(DiscreteController):
    def __init__(self, seed: Optional[int] = None):
        self.seed: Optional[int] = seed

    def control(self, observation: np.ndarray) -> int:
        return policy.control(observation)

if __name__ == '__main__':
    print("Starting simulation")

    repetitions = int(sys.argv[1])
    env = gym.make("2048-v0", render_mode='terminal')

    for rep in range(0, repetitions):
        # random controller: it picks random actions at every step
        controller = Controller()

        protocol = open("protocol-%s.json" % (rep,), "w")
        protocol.write("[")

        # evaluation loop: first reset, then iteration for episode_length steps
        observation, info = env.reset()
        env.render()

        isNotFirst = False
        while True:
            try:
                action = controller.control(observation)
                observation, reward, terminated, truncated, info = env.step(action)
                env.render()

                if isNotFirst:
                    protocol.write(",")
                else:
                    isNotFirst = True

                protocol.write("{")
                protocol.write('"terminated":')
                protocol.write("true" if terminated else "false")
                protocol.write(",")
                protocol.write('"truncated":')
                protocol.write("true" if truncated else "false")

                protocol.write(",")
                protocol.write('"last-direction":')
                protocol.write('"' + info['direction'] + '"')

                protocol.write(",")
                protocol.write('"total-score":')
                protocol.write("%s" % (info['total_score'],))

                protocol.write(",")
                protocol.write('"move-count":')
                protocol.write("%s" % (info['move_count'],))

                protocol.write(",")
                protocol.write('"reward":')
                protocol.write("%s" % (reward,))

                protocol.write(",")
                protocol.write('"highest-tile":')
                protocol.write("%s" % (info['highest_tile'][0],))

                protocol.write("}")

                if terminated or truncated:
                    break
            except:
                print("Failed due to exeption ...")
                break

        protocol.write("]")
        env.close()

    print("Finished")
