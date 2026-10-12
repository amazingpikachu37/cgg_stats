package Stats;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ThreadData {
    public final int id;
    public final UUID uuid;
    public final String name;
    public final String title;
    public final String shortDescription;
    public final String description;
    public final String rules;
    public final String firstValidCount;
    public final String validationType;
    public final boolean resetOnMistakes;
    public final boolean allowDoublePosts;
    public final List<String> visibleTo;
    public final List<String> updatableBy;
    public final boolean locked;
    public final boolean autoValidated;
    public final List<String> moderators;
    public final List<String> verifiers;
    public final List<String> countBans;
    public final List<String> postBans;
    public final String color1;
    public final String color2;
    public final String category;
    public final int countsPerSplit;
    public final int splitsPerGet;
    public final int splitOffset;
    public final boolean threadOfTheDay;
    public final String type;
    public ThreadData(@JsonProperty("id") int id, @JsonProperty("uuid") String uuid, @JsonProperty("name") String name, @JsonProperty("title") String title, @JsonProperty("shortDescription") String shortDescription, @JsonProperty("description") String description, @JsonProperty("rules") String rules, @JsonProperty("firstValidCount") String firstValidCount, @JsonProperty("validationType") String validationType, @JsonProperty("resetOnMistakes") boolean resetOnMistakes, @JsonProperty("allowDoublePosts") boolean allowDoublePosts, @JsonProperty("visibleTo") List<String> visibleTo, @JsonProperty("updatableBy") List<String> updatableBy, @JsonProperty("locked") boolean locked, @JsonProperty("autoValidated") boolean autoValidated, @JsonProperty("moderators") List<String> moderators, @JsonProperty("verifiers") List<String> verifiers, @JsonProperty("countBans") List<String> countBans, @JsonProperty("postBans") List<String> postBans, @JsonProperty("color1") String color1, @JsonProperty("color2") String color2, @JsonProperty("category") String category, @JsonProperty("countsPerSplit") int countsPerSplit, @JsonProperty("splitsPerGet") int splitsPerGet, @JsonProperty("splitOffset") int splitOffset, @JsonProperty("threadOfTheDay") boolean threadOfTheDay, @JsonProperty("type") String type) {
        this.id = id;
        this.uuid = new UUID(uuid);
        this.name = name;
        this.title = title;
        this.shortDescription = shortDescription;
        this.description = description;
        this.rules = rules;
        this.firstValidCount = firstValidCount;
        this.validationType = validationType;
        this.resetOnMistakes = resetOnMistakes;
        this.allowDoublePosts = allowDoublePosts;
        this.visibleTo = visibleTo;
        this.updatableBy = updatableBy;
        this.locked = locked;
        this.autoValidated = autoValidated;
        this.moderators = moderators;
        this.verifiers = verifiers;
        this.countBans = countBans == null ? new ArrayList<>() : countBans;
        this.postBans = postBans == null ? new ArrayList<>() : postBans;
        this.color1 = color1;
        this.color2 = color2;
        this.category = category;
        this.countsPerSplit = countsPerSplit;
        this.splitsPerGet = splitsPerGet;
        this.splitOffset = splitOffset;
        this.threadOfTheDay = threadOfTheDay;
        this.type = type;
    }
    public ThreadData(int id, String name, String title) {
        this.id = id;
        this.uuid = new UUID(0,0);
        this.name = name;
        this.title = title;
        this.shortDescription = null;
        this.description = null;
        this.rules = null;
        this.firstValidCount = null;
        this.validationType = null;
        this.resetOnMistakes = false;
        this.allowDoublePosts = false;
        this.visibleTo = new ArrayList<>();
        this.updatableBy = new ArrayList<>();
        this.locked = false;
        this.autoValidated = false;
        this.moderators = new ArrayList<>();
        this.verifiers = new ArrayList<>();
        this.countBans = new ArrayList<>();
        this.postBans = new ArrayList<>();
        this.color1 = null;
        this.color2 = null;
        this.category = "";
        this.countsPerSplit = 0;
        this.splitsPerGet = 0;
        this.splitOffset = 0;
        this.threadOfTheDay = false;
        this.type = null;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(this.id).append(",");
        sb.append("\"uuid\":\"").append(Util.escapeJava(this.uuid.toString())).append("\",");
        sb.append("\"name\":\"").append(Util.escapeJava(this.name)).append("\",");
        sb.append("\"title\":\"").append(Util.escapeJava(this.title)).append("\",");
        sb.append("\"shortDescription\":\"").append(Util.escapeJava(this.shortDescription)).append("\",");
        sb.append("\"description\":\"").append(Util.escapeJava(this.description)).append("\",");
        sb.append("\"rules\":\"").append(Util.escapeJava(this.rules)).append("\",");
        sb.append("\"firstValidCount\":\"").append(Util.escapeJava(this.firstValidCount)).append("\",");
        sb.append("\"validationType\":\"").append(Util.escapeJava(this.validationType)).append("\",");
        sb.append("\"resetOnMistakes\":\"").append(resetOnMistakes).append("\",");
        sb.append("\"allowDoublePosts\":\"").append(allowDoublePosts).append("\",");
        sb.append("\"visibleTo\":[");
        for (String to : visibleTo) {
            sb.append(Util.escapeJava(to)).append(",");
        }
        sb.append("],");
        sb.append("\"updatableBy\":[");
        for (String by : updatableBy) {
            sb.append(Util.escapeJava(by)).append(",");
        }
        sb.append("],");
        sb.append("\"locked\":\"").append(locked).append("\",");
        sb.append("\"autoValidated\":\"").append(autoValidated).append("\",");
        sb.append("\"moderators\":[");
        for (String moderator : moderators) {
            sb.append(Util.escapeJava(moderator)).append(",");
        }
        sb.append("]");
        sb.append("\"verifiers\":[");
        for (String verifier : verifiers) {
            sb.append(Util.escapeJava(verifier)).append(",");
        }
        sb.append("]");
        sb.append("\"countBans\":[");
        for (String countBan : countBans) {
            sb.append(Util.escapeJava(countBan)).append(",");
        }
        sb.append("]");
        sb.append("\"postBans\":[");
        for (String postBan : postBans) {
            sb.append(Util.escapeJava(postBan)).append(",");
        }
        sb.append("]");
        sb.append("\"color1\":\"").append(Util.escapeJava(this.color1)).append("\",");
        sb.append("\"color2\":\"").append(Util.escapeJava(this.color2)).append("\",");
        sb.append("\"category\":\"").append(Util.escapeJava(this.category)).append("\",");
        sb.append("\"countsPerSplit\":\"").append(countsPerSplit).append("\",");
        sb.append("\"splitsPerGet\":\"").append(splitsPerGet).append("\",");
        sb.append("\"splitOffset\":\"").append(splitOffset).append("\",");
        sb.append("\"threadOfTheDay\":\"").append(threadOfTheDay).append("\",");
        sb.append("\"global\":\"").append(Util.escapeJava(this.category)).append("\"}");
        return sb.toString();
    }
}
