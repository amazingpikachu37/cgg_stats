package Stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;
import java.util.List;

public record CounterData(@JsonProperty("id") int id, @JsonProperty("uuid") String uuid, @JsonProperty("discordId") String discordId, @JsonProperty("name") String name, @JsonProperty("username") String username, @JsonProperty("color") String color, @JsonProperty("roles") List<String> roles, @JsonProperty("avatar") String avatar, @JsonProperty("cardStyle") String cardStyle, @JsonProperty("cardBorderStyle") String cardBorderStyle, @JsonProperty("pronouns") List<String> pronouns, @JsonProperty("title") String title, @JsonProperty("xp") String xp, @JsonProperty("xp2") String xp2, @JsonProperty("rainbow") int rainbow, @JsonProperty("lastRob") String lastRob, @JsonProperty("emoji") String emoji) {
    @Override
    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(this.id).append(",");
        sb.append("\"uuid\":\"").append(Util.escapeJava(this.uuid)).append("\",");
        if (this.discordId == null)
            sb.append("\"discordId\":null,");
        else
            sb.append("\"discordId\":\"").append(Util.escapeJava(this.discordId)).append("\",");
        sb.append("\"name\":\"").append(Util.escapeJava(this.name)).append("\",");
        sb.append("\"username\":\"").append(Util.escapeJava(this.username)).append("\",");
        sb.append("\"color\":\"").append(Util.escapeJava(this.color)).append("\",");
        sb.append("\"roles\":[");
        for (String role : roles) {
            sb.append(Util.escapeJava(role)).append(",");
        }
        sb.append("],");
        if (this.avatar == null)
            sb.append("\"avatar\":null,");
        else
            sb.append("\"avatar\":\"").append(Util.escapeJava(this.avatar)).append("\",");
        if (this.cardStyle == null)
            sb.append("\"cardStyle\":null,");
        else
            sb.append("\"cardStyle\":\"").append(Util.escapeJava(this.cardStyle)).append("\",");
        if (this.cardBorderStyle == null)
            sb.append("\"cardBorderStyle\":null,");
        else
            sb.append("\"cardBorderStyle\":\"").append(Util.escapeJava(this.cardBorderStyle)).append("\",");
        sb.append("\"pronouns\":[");
        for (String pronoun : pronouns) {
            sb.append(Util.escapeJava(pronoun)).append(",");
        }
        sb.append("],");
        if (this.title == null)
            sb.append("\"title\":null,");
        else
            sb.append("\"title\":\"").append(Util.escapeJava(this.title)).append("\",");
        sb.append("\"xp\":\"").append(Util.escapeJava(this.xp)).append("\",");
        sb.append("\"xp2\":\"").append(Util.escapeJava(this.xp2)).append("\",");
        sb.append("\"rainbow\":\"").append(this.rainbow).append("\",");
        if (this.lastRob == null)
            sb.append("\"lastRob\":null,");
        else
            sb.append("\"lastRob\":\"").append(Util.escapeJava(this.lastRob)).append("\",");
        if (this.emoji == null)
            sb.append("\"emoji\":null");
        else
            sb.append("\"emoji\":\"").append(Util.escapeJava(this.emoji));
        sb.append("\"}");
        return sb.toString();
    }

}
