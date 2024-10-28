import matplotlib.pyplot as plt
import numpy as np

# Определение координат для волнистой линии
x = np.linspace(250, 1000, 1000)
y = np.sin(x  0.02)  0.25 + 0.5

# Создание графика
fig, ax = plt.subplots(figsize=(10, 3))

# Отрисовка волнистой линии
ax.plot(x, y, color='black')

# Заливка областей
ax.fill_between(x, y, where=(x >= 250) & (x < 400), color='black')
ax.fill_between(x, y, where=(x >= 400) & (x < 440), color='lightgray')
ax.fill_between(x, y, where=(x >= 440) & (x < 460), color='darkgray')
ax.fill_between(x, y, where=(x >= 460) & (x < 500), color='black')
ax.fill_between(x, y, where=(x >= 500) & (x < 580), color='lightgray')
ax.fill_between(x, y, where=(x >= 580) & (x < 590), color='white')
ax.fill_between(x, y, where=(x >= 590) & (x < 620), color='lightgray')
ax.fill_between(x, y, where=(x >= 620) & (x < 750), color='darkgray')
ax.fill_between(x, y, where=(x >= 750) & (x < 1000), color='black')

# Добавление пунктирных линий
ax.vlines(400, 0, 1, linestyles='dashed', colors='black')
ax.vlines(750, 0, 1, linestyles='dashed', colors='black')

# Подписи по оси абсцисс
ax.set_xticks(np.arange(300, 1001, 100))
ax.set_xlabel('λ/nm')

# Добавление текста
ax.text(300, 0.7, 'UV', color='white', fontsize=16)
ax.text(550, 0.7, 'Visible', color='black', fontsize=16)
ax.text(850, 0.7, 'IR', color='white', fontsize=16)

# Линия со стрелками
ax.plot([400, 750], [0.65, 0.65], color='black', marker='<', markersize=8, markeredgecolor='black', markerfacecolor='black', linestyle='-')

# Горизонтальная линия в центре
ax.hlines(0.5, 250, 1000, color='white')

# Удаление оси ординат
ax.set_yticks([])

# Очистка графика
plt.show()