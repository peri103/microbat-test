from collections import deque, namedtuple

import random

Transition = namedtuple('Transition', ('node_order', 'input', 'action', 'reward'))

class ReplayMemory(object):
    def __init__(self, config):
        self.config = config
        self.capacity = self.config["data.capacity"]
        self.memory = deque([], maxlen=self.capacity)
    
    def push(self, transition):
        self.memory.append(transition)

    # def push(self, *args):
    #     self.memory.append(Transition(*args))

    def sample(self, batch_size):
        return random.sample(self.memory, batch_size)
    
    def __len__(self):
        return len(self.memory)
