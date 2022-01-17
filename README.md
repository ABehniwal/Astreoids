# Astreoids
Simple Game Assignment for ICS 4U

This game is based on the original version of Asteroids.

Created by Amanbir Behniwal

## Controls
In this game, like asteroids, the player starts in the middle and can 
- move forward using the forward arrow key
- rotate right using the right arrow key
- rotate left using the left arrow key
- shoot a bullet by pressing the space bar (a max of 6 bullets can be on the screen at a time)
    
## Description
The objective of the game is to rack up as many points as possible until you die. Points can be accumulated by 
hitting asteroids or hitting the enemy saucers that pop up occasionally. The asteroids come in three sizes. If a big asteroid
is hit, it splits into two medium sized asteroids. Similarly, if a medium sized asteroid is hit, it splits into two small
sized asteroids. Finally, if a small sized asteroid is hit, it will dissapear. Each next level has an additional asteroid
in the beginning and the enemy saucers appear faster as the game progresses. A level cannot be cleared until all the asteroids 
from that level are destroyed. 

## Points
    - Big Asteroid - 20 pts
    - Medium Asteroid - 50 pts
    - Small Asteroid - 100 pts
    - Big Saucer - 200 pts
    - Small Saucer - 1000 pts
    

The big saucer moves randomly through the screen and also shoots randomly. The small saucer will try its best to dodge asteroids
and will shoot directly at the player. The saucers will start from either the left or right side of the screen and make their 
way to the opposite end. The saucers shoot bullets after a certain time limit and if these bullets can break asteroids if they 
are hit. If an asteroid and saucer collide, the asteroid will break/split and the saucer will die. Similarly, if the saucer and 
the player collide, the saucer will die and the player will also lose a life. 
The player starts with 3 lives and whenever the player loses a life, they are spawned in the middle when it is safe to do so
and after a certain amount of time has passed.
