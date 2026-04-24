package InviteMangerSubSystem;

import EventManagementSubSystem.BasicEvent;

import java.time.LocalDateTime;



/*
 * Goal is to implement all interaction of other subsystem through this class 
 * only creating a unified interface for the entire system. However this class
 * in itself must not perform any function other than delegation to the 
 * respective class of any and all interactions possible from other
 * parts of project.
 **/


public class InviteManagerFacade {
    
	
	public static void getInvitedToEvent(BasicEvent basicEvent) {
		// TODO Auto-generated method stub	
	}
	
	

	public static String[] getInvites(int id) {
		return null;
		// TODO Auto-generated method stub
		
	}



	public static void acceptInvite(String invites) {
		// TODO Auto-generated method stub
		
	}



	public static void rejectInvite(String string) {
		// TODO Auto-generated method stub
		
	}



}
