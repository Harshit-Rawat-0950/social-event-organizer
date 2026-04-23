package ReportDashboardSubSystem;

import java.util.ArrayList;
import java.util.List;

import UserMangerSubSystem.ConfigLoader;

public class MainPage implements Page{

    private static MainPage instance;
    static List<TextElement> UIElements;
	private MainPage() {
		ConfigLoader configLoader = new ConfigLoader();
	    List<TextElement> UIElements = new ArrayList<>();
    }

    public static synchronized MainPage getInstance() {
        if (instance == null) {
            instance = new MainPage();
        }
        return instance;
    }	
    public void displayMainPage()
    {
    	for(TextElement text:UIElements)
    	{
    		if(text!=null&&text.isAllowed())
    		text.display();
    	}
    }
}
