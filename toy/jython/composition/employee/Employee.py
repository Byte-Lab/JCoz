# Employee.py

from jyinterface.interfaces import EmployeeType
from jyinterface.interfaces import DependentType


class Employee(EmployeeType):
    def __init__(self, first, last, id):
        self.first = first
        self.last = last
        self.id = id
        deps = self.create_dependents()
        self.deps = deps

    def create_dependents(self):
        d1 = Dependent('Sally', 'Serious', 11)
        d2 = Dependent('Larry', 'Lighthearted', 12)
        return [d1, d2]

    def getEmployeeFirst(self):
        return self.first

    def getEmployeeLast(self):
        return self.last

    def getEmployeeId(self):
        return self.id

    def getDependents(self):
        return self.deps

    def addDependent(self, dependent):
        self.deps.append(dependent)


class Dependent(DependentType):
    def __init__(self, first, last, id):
        self.first = first
        self.last = last
        self.id = id

    def getDependentFirst(self):
        return '<<%s>>' % self.first

    def getDependentLast(self):
        return '<<%s>>' % self.last

    def getDependentId(self):
        return self.id * 4

