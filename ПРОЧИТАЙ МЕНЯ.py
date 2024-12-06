# Fork's Minecraft Botting Framework

from fmbf import AbsoluteSolver
import time

# Создать объект, управляющий ботами Minecraft
solver = AbsoluteSolver(debug=True)

# Создать программу для бота
#def Dev(object, type, distance):
#if object == 'air':
#print('Ты смотришь на воздух')
#else:
#print(f'Ты смотришь на {object} типа {type}, находящемся за {distance} блоков от тебя')
#return 'осмотрись
def Dev():
    return 'иди'

# Связать бота и программу
solver.add(Dev)

### И вуаля! Бот уже работает!

print('hi')
while True:           # added
    time.sleep(1)