CREATE TABLE technical_decisions (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    author VARCHAR(120) NOT NULL,
    decision_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX ix_decisions_project_id ON technical_decisions (project_id);
CREATE INDEX ix_decisions_status ON technical_decisions (status);
