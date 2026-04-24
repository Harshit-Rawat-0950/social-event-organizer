package ReportDashboardSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class TextElement {
	//to be implemented
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
		
		//TODO
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
	public void stylise()
	{
		//TODO
	}
	public boolean isAllowed() {
		// TODO Auto-generated method stub
		if(new ConfigLoader().getProperty("app.mode").equals("dev"))
			return true;
		else if(devMode)
			return false;
		return true;
	}
	public static List<TextElement> readFile(String filePath)
	{
		Path textFilePath = Paths.get(filePath);
		List<TextElement> res = new ArrayList<>();
		try {
            List<String> lines = Files.readAllLines(textFilePath);
            for (String line : lines) {
            	String[] part = line.split(",");
            	res.add(new TextElement(part[0],Integer.parseInt(part[1]),Boolean.parseBoolean(part[2]),Boolean.parseBoolean(part[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
		return res;
	}
}
