package ReportDashboardSubSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class TextElement {
	//to be implemented
	String Content = "";
	int style;
	boolean DevMode;
	boolean NewLine;
	public TextElement(String Content, int style, boolean Newline, boolean DevMode)
	{
		
	}
	public void display()
	{
		//TODO
	}
	public static void displayList(List<TextElement> page)
	{
		for (TextElement text:page)
		{
			text.display();
			//if(text.)
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
		else if(DevMode)
			return false;
		return true;
	}
	public List<TextElement> readFile(Path filePath)
	{
		List<TextElement> res = new ArrayList<>();
		try {
            List<String> lines = Files.readAllLines(filePath);
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
