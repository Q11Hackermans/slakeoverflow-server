# Slakeoverflow Server Documentation

## What is Slakeoverflow?
Slakeoverflow is a multiplayer snake game.  
This documentation explains how to use the server.

## Game Management

### Game information
To get information about a running game, you can use `game info`.  
You will get the following information:
- Game state (+ manual ticks)
- Player and item count
- World border

### Start game
To start a game, the `game start` command can be used.  
With `game start default` a game can be set with the default size defined in the config.  
With `game start automatic` the game size can be determined automatically based on the current number of server connections.  
With `game start custom <X> <Y>` the game size can be set manually. `<X>` is the world border at the bottom and `<Y>` is the world border on the right.  
The worldborder left and top is always `X=0` and `Y=0`.  
With `game start savegame <SAVEGAME>` a game state can be loaded, which was exported before over `game getsave`.

### Stop game
To stop the game, the `game stop` command can be used.

### Pause and resume game
When a game is paused, the server will not run the game session's tick. This means that all checks of the game session (and snakes) are disabled.  
Snakes will not be cleaned up while the game is paused and the connection of the snake gets closed, new player connections won't get new snakes and of course there is no player movement or any other thing the game session is responsible for.  
To pause a running game, use `game pause`.
To resume a paused game, use `game resume`.

### Manual game control
If a game is paused, you can manually run a specific amount of ticks.  
You can do this with the `game mcontrol` command.  
`game mcontrol runtick` runs a single tick.  
`game mcontrol runticks <count>` runs the specified amount of ticks.  
`game mcontrol stop/clear/remove` will cancel the running of manual ticks.  
You can see the amount of manual ticks in the `game info` command.

### Save and load game
#### Save
You can save and load a game. To get the savegame (JSON String), use the command `game getsave`. You need to copy the string from the console and save it to a text file. Don't change the values (or only if you know what you are doing).

#### Load
You can load the game via the `game start savegame <SAVEGAME STRING>` command. After you have loaded the savegame, the game is paused. It is now required to set new connections to the snakes via the `game modify snake <ID> connection <UUID>` command to prevent them getting cleaned up when resuming game. If you have done that, you can resume the game via `game resume` command.

### Get game values
You can get the following game values:
- Snake list (`game get snakes`)
- All snake values (`game get snake`)
- Item list with coordinates, type, despawn time and food value (`game get items`)
- World border (`game get border`)
- FOV (`game get fov`)

### Modify game values
There is also a way to modify the game values.  
The `<UUID>` argument means the connection UUID.  
The `<ID>` argument means the snake/item id.

#### Snake modifications
- Kill snake (`game modify snake <UUID/ID> kill`)
- Modify snake position / teleport snake (`game modify snake <UUID/ID> position <X> <Y>`)
- Add snake bodies / add snake length (`game modify snake <UUID/ID> bodies add <amount>`)
- Remove snake bodies / remove snake length (`game modify snake <UUID/ID> bodies remove <amount>`)
- Clear snake bodies / set snake length to 0 (`game modify snake <UUID/ID> bodies clear`)
- Force-change snake's facing (changing the facing into the opposite direction is not possible) (`game modify snake <UUID/ID> facing <N=0,S=2,E=1,W=3>`)
- Change the connection / user of a snake (`game modify snake <UUID/ID> connection <UUID>`)

#### Item modifications
- Kill item (`game modify item <ID> kill`)
- Modify item position / teleport item (`game modify item <ID> position <X> <Y>`)
- Modify item despawn time (`game modify item <ID> despawn <despawnTime>`)
- Kill all items (`game modify items kill`)

## User Management
Slakeoverflow splits a user in 3 parts:
- Connection / Client (= CMSClient by ConnectionManager)
- User / ServerConnection (= Slakeoverflow connections)
- Snakes (= Snakes in GameSession)

These different types can be managed with 3 different commands.

#### Connections / Clients (CMSClients)
With the `connection` command, you manage the ConnectionManager connections. It allows you to do the following things:
- List connections (`connection list`)
- Get connection information (`connection info <UUID>`)
- Close connections (`connection close <UUID>`)
- Accept and deny pending connections (`connection accept/deny <UUID>`)

##### Pending connections
Connections / Clients (CMSClients) are part of ConnectionManager.  
If a client connects to the server, it will be a pending connection until the server accepts the connection. Pending connections can be listed with `connection list-pending`. If the config option `auto_accept_connections` is set to true, the server will automatically accept connections. If not, the user has to do this manually.

The server will accept connections like this:
- Is the IP blacklisted? YES --> DENY, ELSE continue
- Are max connections reached? YES --> Request user input (auto deny in 5 seconds), ELSE continue
- Is autoConnectionAccept enabled? YES --> Accept connection, ELSE --> Request user input (auto deny in 10 seconds)

If a pending connection gets accepted, it will be an established connection.

##### Established connections
Connections which are accepted are established connections. You can list them via `connection list-established`. Established connections can communicate, send requests and will receive status messages. The ServerConnections are synced with the established connections.

#### Users / ServerConnections (Slakeoverflow connections)
As far as the users/ServerConnections are synced with the established connections of the ConnectionManager, every user/ServerConnection has its own connection/client.  
Users/ServerConnections are managed by the `users` command with which you can do the following things:
- List users (Should be list the same connections as `connection list-established`, but with some more information) (`user list`)
- Get user info (`user info <UUID>`)
- Authenticate user (`user auth <UUID> player/spectator`)
- Unauthenticate user (`user unauth <UUID>`)
- Login user into an account (`user login <UUID> <accountID>`)
- Logout user from an account (`user logout <UUID>`)

##### User authentication states
Users can be in 3 different states: UNAUTHENTICATED, PLAYER and SPECTATOR.

In UNAUTHENTICATED state, the user will see the lobby page on his client. On this page, he can choose if he want to be a player or a spectator. He also can login into his account.

In PLAYER state, the user will have a snake and see the game page on the client. He can play the game. The user will receive playerstate packages. If the user's snake dies in game and `unauthenticate_player_on_death` is set to true, the player gets unauthenticated automatically. Else the player will stay in PLAYER state and get a new snake.

In SPECTATOR state, the user will see the spectator page on the client (currently not supported by the client). The user will receive spectator packages.

A user will set into the UNAUTHENTICATED state if no game is running.  
The authentication by the user from the client can be disabled in config with the setting `allow_user_authentication`.  
If user authentication is disabled, only the server can auth/unauth the user via the `user auth/unauth` command.

##### User accounts
A user can log in into an account. The user stays logged in while the user is connected. The log in by user can be disabled in config via `allow_login` option.  
Logins also can be set via console with the `user login/logout` command. The account system (user login/account) is independent from the authentication system (user authentication state).

#### Snakes
If a game is running (not paused), all users which are in authentication state PLAYER will have their snake. In the snake, all data for this user for the current running game is saved. If a snake dies, it will get deleted. If `unauthenticate_player_on_death` is set to true, the player get unauthenticated automatically. Else the player remains in PLAYER auth state and will get a new snake automatically. The snakes can be managed by the `game modify snake` command (see game management).

## Config
The configuration is used to configure the server.
Config values can be set via the config.json file created on the first start of the server or via the config command in console.

### Server configuration options

#### server_name
Default: "Slakeoverflow-Server"
  
The name of the server. It will be send to the client when it requrests the server information.

#### max_connections
Default: 20
  
The limit of connections to the server.
If the limit is reached and a new client tries to connect, the connection can be accepted within the next 10 seconds in console until it gets disconnected.

#### auto_connection_accept
Default: true
  
If this option is enabled, new connections are automatically accepted.
If this opton is disabled, new connections have to be accepted via console within 10 seconds after connecting. Else, the connection gets closed.

#### allow_guests
Default: true
  
If this option is disabled, only clients which are logged in can authenticate as player or spectator.
If this option is enabled, every client can authenticate even if it's not logged in.

#### port
Default: 26677
  
The port the server runs on.

#### allow_registration
Default: true
  
If this option is enabled, clients can register accounts.
If not, the account registration is disabled.

#### user_authentication
Default: true
  
If this is enabled, users (clients) can authenticate as player or spectator.
If this is disabled, only admins and privileged users (ADMIN/MODERATOR) can authenticate and users can only be authenticated via console.

#### allow_login
Default: true
  
If this option is disabled, users cannot log in (except admins and moderators).
Together with ``also_disable_privileged_login`` and ``allow_registration``, this enables/disables the account system.

#### also_disable_privileged_login
Default: false
  
This option only works if allow_login is set to ``false``.
If this option is enabled and ``allow_login`` is disabled, even admins and moderators cannot login anymore.
Use this option together with ``allow_registration`` and ``allow_login`` on ``false`` to completely disable the account system.

#### unauthenticate_player_on_death
Default: true
  
If this option is enabled, a user whose snake dies will be unauthenticated (back to main menu).
If this option is disabled, the snake of this user will instantly respawn.

#### print_debug_messages
Default: false
  
If this option is enabled, the server will show optional debug messages.

### Game configuration options

#### max_players
Default: 20
  
This is the count of the maximum player count (NOT CONNECTIONS!).
Clients can still connect to the server, but they can't authenticate as player if the limit is reached.
Privileged users can still authenticate, and users can still be authenticated by console.

#### max_spectators
Default: 2
  
Same as ``max_players``, but only for the spectators.

#### min_food_value, max_food_level

If the snake eats food, its length will be increased by the food level.
Food will be spawned with a level between ``min_food_value`` and ``max_food_value``.

#### default_snake_length
Default: 2
  
The length a snake will be spawned with.

#### snake_speed_base, snake_speed_modifier_value, snake_speed_modifier_bodycount
Default: 2, 1, 5
  
The snake movement system technically counts down a "move in score".
If the move in score is 0, it will be reset to the current snake speed and the snake will move.
  
The current snake speed is calculated with ``snake_speed_base + (snake_length / snake_speed_modifier_bodycount) * snake_speed_modifier_value`` (``snake_length`` is the current snake length).
  
You can understand this formula like that: Every snake has a speed of ``snake_speed_base``.
Every ``snake_speed_modifier_bodycount`` bodies (= length), the snake speed will get ``snake_speed_modifier_value`` slower.
  
To only use the ``snake_speed_base`` as persistent speed, you can set ``snake_speed_modifier_value`` to 0.

#### default_gamefield_size_x, default_gamefield_size_y
Default: 100, 100
  
This is the gamefield size which is used if you start a game with ``game start default``.
This is not so important because you also can start a game with ``game start custom <size_x> <size_y>``.

#### default_item_despawn_time, item_superfood_despawn_time
Default: 60, 120
  
This is the item despawn time.
A default item despawns after ``default_item_despawn_time`` seconds, superfood (that what a snake drops if it dies) despawns after ``item_superfood_despawn_time`` seconds.
Please note that the times are only seconds if the server has a normal server tickrate (20 ticks/second)

#### enable_spectator
Default: true
  
If this option is enabled, users can (be) authenticate(d) as spectator.
If not, the spectator mode won't work.

#### spectator_update_interval
Default: 200
  
This is the time (in ticks) the spectator data will be updated.
Please note that creating spectator data requires performance and a too low update interval leads to server lags.

#### enable_snake_speed_boost
Default: true
  
If this is enabled, the snake speed boost can be used by pressing SPACE.
If a snake uses the speed boost, it consumes its own body (length) while getting an acceleration.
This means, that a multiplier (and not 1 anymore) gets subtracted from the snake's "move in score".
This multiplier increases by every consumed snake body unit.

#### eat_own_snake
Default: true

If this is enabled and a snake hits one of its body units with its head, the snake bodies behind this body unit are getting cut off.
If this is disabled and a snake hits one of its body units with its head, the snake dies like it hits another snake's bodies.

