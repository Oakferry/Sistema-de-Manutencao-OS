PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS usuario (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    login TEXT NOT NULL UNIQUE,
    perfil TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL UNIQUE,
    disponibilidade TEXT NOT NULL,

    FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS equipamento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome TEXT NOT NULL,
    descricao TEXT NOT NULL,
    tipo TEXT NOT NULL,
    identificador TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS ordem_servico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    equipamento_id INTEGER NOT NULL,
    usuario_abertura_id INTEGER NOT NULL,
    tecnico_responsavel_id INTEGER,
    descricao_problema TEXT NOT NULL,
    criticidade TEXT NOT NULL,
    status TEXT NOT NULL,
    data_abertura TEXT NOT NULL,
    data_encerramento TEXT,

    FOREIGN KEY (equipamento_id)
        REFERENCES equipamento(id),

    FOREIGN KEY (usuario_abertura_id)
        REFERENCES usuario(id),

    FOREIGN KEY (tecnico_responsavel_id)
        REFERENCES tecnico(id)
);

CREATE TABLE IF NOT EXISTS diagnostico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL UNIQUE,
    tecnico_id INTEGER NOT NULL,
    parecer TEXT NOT NULL,
    causa_raiz TEXT NOT NULL,
    data_hora TEXT NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id)
);

CREATE TABLE IF NOT EXISTS intervencao (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL,
    tecnico_id INTEGER NOT NULL,
    descricao TEXT NOT NULL,
    horas_trabalhadas REAL NOT NULL,
    data_hora TEXT NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id)
);

CREATE TABLE IF NOT EXISTS material_utilizado (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    intervencao_id INTEGER NOT NULL,
    descricao TEXT NOT NULL,
    quantidade REAL NOT NULL,
    unidade TEXT NOT NULL,

    FOREIGN KEY (intervencao_id)
        REFERENCES intervencao(id)
);

CREATE TABLE IF NOT EXISTS atribuicao_tecnico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL,
    tecnico_id INTEGER NOT NULL,
    atribuido_por_id INTEGER NOT NULL,
    data_atribuicao TEXT NOT NULL,
    data_fim TEXT,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (tecnico_id)
        REFERENCES tecnico(id),

    FOREIGN KEY (atribuido_por_id)
        REFERENCES usuario(id)
);

CREATE TABLE IF NOT EXISTS historico_status_os (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    ordem_servico_id INTEGER NOT NULL,
    status_anterior TEXT,
    status_novo TEXT NOT NULL,
    data_hora TEXT NOT NULL,
    usuario_id INTEGER NOT NULL,

    FOREIGN KEY (ordem_servico_id)
        REFERENCES ordem_servico(id),

    FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);