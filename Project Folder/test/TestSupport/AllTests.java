package TestSupport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Everything in one run. SuiteCompletenessTest fails if a test class is missing here.
@RunWith(Suite.class)
@Suite.SuiteClasses({
    EventManagementSubSystem.BasicEventTest.class,
    EventManagementSubSystem.CompositeEventTest.class,
    EventManagementSubSystem.EventComponentTest.class,
    EventManagementSubSystem.EventDecoratorTest.class,
    EventManagementSubSystem.MainEventManagerTest.class,
    EventManagementSubSystem.EventAccessPolicyTest.class,
    EventManagementSubSystem.EventVisibilityMatrixTest.class,
    EventManagementSubSystem.EventFacadeTest.class,
    FriendSubSystem.FriendRepoTest.class,
    FriendSubSystem.FriendFacadeTest.class,
    FriendSubSystem.FriendPairStatesTest.class,
    InviteMangerSubSystem.InviteSubSystemTest.class,
    InviteMangerSubSystem.InviteFacadeTest.class,
    InviteMangerSubSystem.RsvpRepositoryTest.class,
    NotificationSubSystem.NotificationRepoTest.class,
    NotificationSubSystem.NotificationTest.class,
    NotificationSubSystem.NotificationFacadeTest.class,
    UserMangerSubSystem.UserTest.class,
    UserMangerSubSystem.PasswordTest.class,
    UserMangerSubSystem.UserFacadeTest.class,
    UserMangerSubSystem.UserLoginTest.class,
    UserMangerSubSystem.UserRegistrationTest.class,
    UserMangerSubSystem.ConfigLoaderTest.class,
    UserMangerSubSystem.SystemDataServiceTest.class,
    UserMangerSubSystem.AppFlowTest.class,
    ReportDashboardSubSystem.ConsoleInputTest.class,
    ReportDashboardSubSystem.StyleTest.class,
    ReportDashboardSubSystem.ThemeTest.class,
    ReportDashboardSubSystem.TerminalTest.class,
    ReportDashboardSubSystem.WidgetsTest.class,
    ReportDashboardSubSystem.EventCardRendererTest.class,
    ReportDashboardSubSystem.TextElementTest.class,
    ReportDashboardSubSystem.ScreenTest.class,
    ReportDashboardSubSystem.DashboardFacadeTest.class,
    ReportDashboardSubSystem.MainPageFlowTest.class,
    ReportDashboardSubSystem.EventPageFlowTest.class,
    ReportDashboardSubSystem.InvitesPageFlowTest.class,
    ReportDashboardSubSystem.NotificationPageFlowTest.class,
    ReportDashboardSubSystem.ReportPageFlowTest.class,
    ReportDashboardSubSystem.FriendsPageFlowTest.class,
    ReportDashboardSubSystem.ProfilePageFlowTest.class,
    TestSupport.SuiteCompletenessTest.class,
})
public class AllTests {
}
