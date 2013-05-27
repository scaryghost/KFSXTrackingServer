PRAGMA foreign_keys = ON;
CREATE TABLE difficulty (id INTEGER PRIMARY KEY NOT NULL UNIQUE, name TEXT NOT NULL, length TEXT NOT NULL,  
    wins INTEGER NOT NULL DEFAULT 0, losses INTEGER NOT NULL DEFAULT 0, waveaccum INTEGER NOT NULL DEFAULT 0,  
    time INTEGER NOT NULL DEFAULT 0);
CREATE TABLE level (id INTEGER PRIMARY KEY NOT NULL UNIQUE, name TEXT NOT NULL UNIQUE, wins INTEGER NOT NULL DEFAULT 0,  
    losses INTEGER NOT NULL DEFAULT 0,  time INTEGER NOT NULL DEFAULT 0)  ;
CREATE TABLE level_difficulty_join(difficulty_id INTEGER NOT NULL, level_id INTEGER NOT NULL, wins INTEGER NOT NULL DEFAULT 0,  
    losses INTEGER NOT NULL DEFAULT 0, waveaccum INTEGER NOT NULL DEFAULT 0, time INTEGER NOT NULL DEFAULT 0,  
    FOREIGN KEY(difficulty_id) REFERENCES difficulty(id),  FOREIGN KEY(level_id) REFERENCES level(id));
CREATE TABLE wave_data (difficulty_id INTEGER NOT NULL, level_id INTEGER NOT NULL, wave INTEGER NOT NULL, category TEXT NOT NULL,  
    stat TEXT NOT NULL, value INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(difficulty_id) REFERENCES difficulty(id),  
    FOREIGN KEY(level_id) REFERENCES level(id));
CREATE TABLE record (id INTEGER PRIMARY KEY NOT NULL UNIQUE, steamid64 TEXT NOT NULL UNIQUE, wins INTEGER NOT NULL DEFAULT 0,  
    losses INTEGER NOT NULL DEFAULT 0, disconnects INTEGER NOT NULL DEFAULT 0,  finales_played INTEGER NOT NULL DEFAULT 0,  
    finales_survived INTEGER NOT NULL DEFAULT 0, time_connected INTEGER NOT NULL DEFAULT 0);
CREATE TABLE player (record_id INTEGER NOT NULL, category TEXT NOT NULL, stat TEXT NOT NULL,  value INTEGER NOT NULL DEFAULT 0,  
    FOREIGN KEY(record_id) REFERENCES record(id))  ;
CREATE TABLE match_history (record_id INTEGER NOT NULL, level_id INTEGER NOT NULL, difficulty_id INTEGER NOT NULL, result TEXT NOT NULL,  
    wave INTEGER NOT NULL, duration INTEGER NOT NULL, timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  
    FOREIGN KEY(record_id) REFERENCES record(id),  FOREIGN KEY(difficulty_id) REFERENCES difficulty(id),  
    FOREIGN KEY(level_id) REFERENCES level(id));
CREATE TABLE steam_info (record_id INTEGER PRIMARY KEY NOT NULL UNIQUE, name TEXT, avatar TEXT,  
    FOREIGN KEY(record_id) REFERENCES record(id));
CREATE TABLE aggregate (category TEXT NOT NULL, stat TEXT NOT NULL, value INTEGER NOT NULL DEFAULT 0);
CREATE UNIQUE INDEX aggregate_ui ON aggregate (category ASC, stat ASC);
CREATE UNIQUE INDEX difficulty_ui ON difficulty (name ASC, length ASC);
CREATE UNIQUE INDEX player_ui ON player (record_id ASC, category ASC, stat ASC);
CREATE UNIQUE INDEX match_history_ui ON match_history (record_id ASC, timestamp ASC) ;
CREATE UNIQUE INDEX wavedata_ui ON wave_data (difficulty_id ASC, level_id ASC, wave ASC, category ASC, stat ASC)   ;
CREATE UNIQUE INDEX level_difficulty_join_ui ON level_difficulty_join (difficulty_id ASC, level_id ASC);


