# Employee.py

from jyinterface.interfaces import Worker 
import random

toy_iters = 1000000

class Worker(Worker):
    def __init__(self, id):
        self.id = id

    def call(self):
        fullRange = range(0, toy_iters)
        sum = random.random()
        incr = random.random()
        for x in fullRange:
          sum += incr

        return None

