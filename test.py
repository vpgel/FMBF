from fmbf import AbsoluteSolver
import time

# Создать объект, управляющий ботами Minecraft
solver = AbsoluteSolver(debug=True)

# Создать программу для бота
def program():
    return 'walk_forward'

# Связать бота и программу
solver.add('Dev', program)

### И вуаля! Бот уже работает!
