#!/usr/bin/python

import threading
import random


toy_iters = 500000


class worker (threading.Thread):
    def __init__(self, threadID, name):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
    def run(self):
        do_work()
            

def do_work():
    sum = random.random()
    for x in range (0, toy_iters):
        sum += random.random()
    return random

def log_loop_finish():
    print "Loop iteration finished"

while (1):
    print "Starting loop"
    n_workers = 15
    threads = []
    for x in range (0, n_workers):
        threads.append(worker(x, "Thread-" + str(x)))
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    
    # Do single threaded work
    do_work()

    log_loop_finish()
    

print "Exiting Main Thread"

