from fmbf import MinecraftConnection

import time

def data_handler(**args):
    return 'move_forward'
start = time.time()
mc = MinecraftConnection(name='Test23', handler=data_handler)
while True:
    if time.time()-start > 100:
        mc.close()