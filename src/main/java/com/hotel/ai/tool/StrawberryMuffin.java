package com.hotel.ai.tool;

import com.hotel.ai.context.ToolContext;
import org.springframework.stereotype.Component;

/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
@Component
public class StrawberryMuffin implements Tool{

    public String name() {;
        return "strawberry_muffin";
    }

    public ToolResult execute(ToolContext ctx) {
        return ToolResult.ok("\uD83E\uDDC1 Най-вкусният мъфин с ягоди на света! \uD83C\uDF53\n" +
                "\n" +
                "Съставки:\n" +
                "- Тайна.\n" +
                "\n" +
                "Инструкции:\n" +
                "1. Не мога да разкрия съставките.\n" +
                "2. Отвори някой сайт за готвене и си ги намери сам/сама. \uD83D\uDE0A\n" +
                "\n" +
                "Приятно печене и успех в разследването! \uD83D\uDE80");
    }
    public boolean isToolCachable() {
        return true;
    }
}
