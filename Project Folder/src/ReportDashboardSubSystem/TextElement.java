package ReportDashboardSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ReportDashboardSubSystem.Theme.Role;
import UserMangerSubSystem.ConfigLoader;

public class TextElement {
	public static final int PLAIN = 0;
	public static final int HEADING = 1;
	public static final int LIST_ITEM = 2;
	public static final int MENU = 3;
	public static final int MUTED = 4;

	private static final Pattern HOTKEY = Pattern.compile("\\([^()\\s]\\)|^\\d+\\.");

	String content = "";
	int style;
	boolean devMode;
	boolean newLine;
	public TextElement(String content, int style, boolean newLine, boolean devMode)
	{
		this.content=content;
		this.style=style;
		this.newLine=newLine;
		this.devMode=devMode;
	}
	public void display()
	{
		System.out.print(stylise());
		if(this.newLine)
			System.out.println();
	}
	public static void displayList(List<TextElement> page)
	{
		for (TextElement text:page)
		{
			if(text!=null&&text.isAllowed())
    			text.display();
		}
	}
	public String stylise()
	{
		switch (this.style) {
			case HEADING:
				return String.join(System.lineSeparator(), Widgets.titleBar(this.content));
			case LIST_ITEM:
				return "   " + Style.color(Role.TEXT, this.content);
			case MENU:
				return " " + styleMenu(this.content);
			case MUTED:
				return " " + Style.color(Role.MUTED, this.content);
			default:
				return this.content;
		}
	}
	// Picks out hotkeys such as (c) or 1. in the accent colour
	static String styleMenu(String text)
	{
		Matcher hotkey = HOTKEY.matcher(text);
		StringBuilder result = new StringBuilder();
		int position = 0;
		while(hotkey.find())
		{
			result.append(Style.color(Role.TEXT, text.substring(position, hotkey.start())));
			result.append(Style.bold(Role.ACCENT, hotkey.group()));
			position = hotkey.end();
		}
		result.append(Style.color(Role.TEXT, text.substring(position)));
		return result.toString();
	}
	// Same element with different text, for filling in live values
	public TextElement withContent(String newContent)
	{
		return new TextElement(newContent, this.style, this.newLine, this.devMode);
	}
	public String getContent()
	{
		return this.content;
	}
	public boolean isAllowed() {
		if("dev".equals(new ConfigLoader().getProperty("app.mode")))
			return true;
		else if(devMode)
			return false;
		return true;
	}
	// Each line is content,style,newLine,devMode. The content itself may contain commas.
	public static List<TextElement> readFile(String filePath)
	{
		Path textFilePath = Paths.get(filePath);
		List<TextElement> res = new ArrayList<>();
		try {
            List<String> lines = Files.readAllLines(textFilePath);
            for (String line : lines) {
            	if(line.trim().isEmpty())
            		continue;
            	int devStart = line.lastIndexOf(',');
            	int newLineStart = line.lastIndexOf(',', devStart - 1);
            	int styleStart = line.lastIndexOf(',', newLineStart - 1);
            	res.add(new TextElement(line.substring(0, styleStart),
            			Integer.parseInt(line.substring(styleStart + 1, newLineStart).trim()),
            			Boolean.parseBoolean(line.substring(newLineStart + 1, devStart).trim()),
            			Boolean.parseBoolean(line.substring(devStart + 1).trim())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return res;
	}
}
