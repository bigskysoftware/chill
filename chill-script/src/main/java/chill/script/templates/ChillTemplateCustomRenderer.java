package chill.script.templates;

import chill.script.templates.commands.ChillTemplateCommand;

import java.util.List;

public interface ChillTemplateCustomRenderer {
    void render(ChillTemplateRuntime context, List<ChillTemplateCommand> body);
}
