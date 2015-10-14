/*
 * JAVA User Interface
 * =============================
 * Name: Kevin Chang
 * Yvette Hernandez
 * Group: 27
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

	// storing user for later use
	private static String loggedinUser = null;

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
	 if(outputHeader){
	    for(int i = 1; i <= numCol; i++){
		System.out.print(rsmd.getColumnName(i) + "\t");
	    }
	    System.out.println();
	    outputHeader = false;
	 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
          List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("\nLOGIN MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Goto Friend List");
                System.out.println("2. Update Profile");
                System.out.println("3. Write a new message");
                System.out.println("4. Send Friend Request");
				System.out.println("5. View Profile");
				System.out.println("6. View Messages");
				System.out.println("7. Change Password");
				System.out.println("8. Search People");
				System.out.println("9. View Requests");
                System.out.println("10. Log out");
                switch (readChoice()){
                   case 1: FriendList(esql); break;
                   case 2: UpdateProfile(esql); break;
                   case 3: NewMessage(esql); break;
                   case 4: SendRequest(esql); break;
				   case 5: ViewProfile(esql); break;
				   case 6: ViewMessages(esql); break;
				   case 7: ChangePassword(esql); break;
				   case 8: SearchPeople(esql); break;
				   case 9: ViewRequests(esql); break;
                   case 10: 
						usermenu = false;
						loggedinUser = null;
						break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

	 //Creating empty contact\block lists for a user
	 String query = String.format("INSERT INTO USR (userId, password, email) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         loggedinUser = login;
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
		if (userNum > 0)
			return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// HELPER FUNCTION for FriendList
	private static void FriendListHelper(ProfNetwork esql, String uid, String parent) 
	{
		try{
			int menu = 1;
			while(menu == 1)
			{
				System.out.print("\n" + uid + "'s Friends List\n");
				System.out.print("------------\n");
				System.out.print("0. Go back to " + parent + "'s Friends List\n");
				System.out.print("1. View Friends\n");
				
				int choice = readChoice();
				switch(choice)
				{
					case 0:
						menu = 0;
						break;
					case 1:
						String view_friends_query = String.format("SELECT * FROM CONNECTION_USR WHERE userId = '%s' AND status='Accept'", uid);
						List<List<String>>userlist1 = esql.executeQueryAndReturnResult(view_friends_query);
						
						String view_friends_query2 = String.format("SELECT * FROM CONNECTION_USR WHERE connectionId = '%s' AND status='Accept'", uid);
						List<List<String>>userlist2 = esql.executeQueryAndReturnResult(view_friends_query2);
						
						// Checking if no friends
						if((userlist1.size() == 0) && (userlist2.size()==0))
						{
							System.out.print(uid + "has no friend connections.");
							return;
						}
						
						System.out.print("\nUserId\t\n");
						for(int row = 0; row < userlist1.size(); row++)
						{
							System.out.print("\t" + userlist1.get(row).get(1) + "\n");
						}
						
						for(int row = 0; row < userlist2.size(); row++)
						{
							System.out.print("\t" + userlist2.get(row).get(0) + "\n");
						}
						
						System.out.print("0. Go back to " + parent + "'s Friends List\n");
						System.out.print("1. View Friend Profile\n");
						System.out.print("2. View Friends of Friends\n");
						System.out.print("\n");
						int choice2 = readChoice();
						switch(choice2)
						{
							case 0:
								return;
							case 1:
								System.out.print("Enter userId of friend: ");
								String uid_input = in.readLine();
								viewProfileHelper(esql, uid_input);
								break;
							case 2:
								System.out.print("\nEnter userId of friend: ");
								String uid_input2 = in.readLine();
								FriendListHelper(esql, uid_input2, uid);
								break;
							default:
								System.out.println("Did not recognize choice.\n");
								break;
						}
						
						//FriendList();
						break;
					default:
						System.out.println("Did not recognize choice.\n");
						break;
				}
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}

// *** Rest of the functions definition go in here
	public static void FriendList(ProfNetwork esql)	
	{
		try{
			int menu = 1;
			while(menu == 1)
			{
				System.out.print("\n" + loggedinUser + "'s Friends List\n");
				System.out.print("------------\n");
				System.out.print("0. Go back to Menu\n");
				System.out.print("1. View Friends\n");
				
				int choice = readChoice();
				switch(choice)
				{
					case 0:
						menu = 0;
						break;
					case 1:
						String view_friends_query = String.format("SELECT * FROM CONNECTION_USR WHERE userId = '%s' AND status='Accept'", loggedinUser);
						List<List<String>>userlist1 = esql.executeQueryAndReturnResult(view_friends_query);
						
						String view_friends_query2 = String.format("SELECT * FROM CONNECTION_USR WHERE connectionId = '%s' AND status='Accept'", loggedinUser);
						List<List<String>>userlist2 = esql.executeQueryAndReturnResult(view_friends_query2);
						
						System.out.print("\nUserId\t\n");
						for(int row = 0; row < userlist1.size(); row++)
						{
							System.out.print("\t" + userlist1.get(row).get(1) + "\n");
						}
						
						for(int row = 0; row < userlist2.size(); row++)
						{
							System.out.print("\t" + userlist2.get(row).get(0) + "\n");
						}
						
						System.out.print("\n0. Go back to Menu\n");
						System.out.print("1. View Friend Profile\n");
						System.out.print("2. View Friends of Friends\n");
						System.out.print("\n");
						int choice2 = readChoice();
						switch(choice2)
						{
							case 0:
								return;
							case 1:
								System.out.print("Enter userId of friend: ");
								String uid_input = in.readLine();
								viewProfileHelper(esql, uid_input);
								break;
							case 2:
								System.out.print("\nEnter userId of friend: ");
								String uid_input2 = in.readLine();
								FriendListHelper(esql, uid_input2, loggedinUser);
								break;
							default:
								System.out.println("Did not recognize choice.\n");
								break;
						}
						
						//FriendList();
						break;
					default:
						System.out.println("Did not recognize choice.\n");
						break;
				}
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end FriendList

	public static void UpdateProfile(ProfNetwork esql)
	{
		try{
			System.out.println("\tUPDATE PROFILE");
			System.out.println("\t--------------");
			System.out.println("\t\t0. Go back to Menu");
			System.out.println("\t\tWork Experience:");
			System.out.println("\t\t\t1. Update Company");
			System.out.println("\t\t\t2. Update Role");
			System.out.println("\t\t\t3. Update Location");
			System.out.println("\t\t\t4. Update Start Date");
			System.out.println("\t\t\t5. Update End Date");
			System.out.println("\t\tEducational Details:");
			System.out.println("\t\t\t6. Update Institution Name");
			System.out.println("\t\t\t7. Update Major");
			System.out.println("\t\t\t8. Update Degree");
			System.out.println("\t\t\t9. Update Start Year");
			System.out.println("\t\t\t10. Update End Year");
			
			int choice = readChoice();
			switch (choice)
			{
				case 0:
					break;
				// WORK EXPERIENCE
				case 1:
					System.out.print("\tEnter Company: ");
					String new_company = in.readLine();
					
					String comp_query = String.format("UPDATE WORK_EXPR SET company='%s' WHERE userId='%s'", new_company, loggedinUser);
			        esql.executeUpdate(comp_query);
					break;
				case 2:
					System.out.print("\tEnter Role: ");
					String new_role = in.readLine();
					
					String role_query = String.format("UPDATE WORK_EXPR SET role='%s' WHERE userId='%s'", new_role, loggedinUser);
			        esql.executeUpdate(role_query);
					break;
				case 3:
					System.out.print("\tEnter Location: ");
					String new_location = in.readLine();
					
					String loc_query = String.format("UPDATE WORK_EXPR SET location='%s' WHERE userId='%s'", new_location, loggedinUser);
			        esql.executeUpdate(loc_query);
					break;
				case 4:
					System.out.print("\tEnter Start Date (yyyy/mm/dd): ");
					String new_start_date = in.readLine();
					
					String stdate_query = String.format("UPDATE WORK_EXPR SET startDate='%s' WHERE userId='%s'", new_start_date, loggedinUser);
			        esql.executeUpdate(stdate_query);
					break;
				case 5:
					System.out.print("\tEnter End Date (yyyy/mm/dd): ");
					String new_end_date = in.readLine();
					
					String enddate_query = String.format("UPDATE WORK_EXPR SET endDate='%s' WHERE userId='%s'", new_end_date, loggedinUser);
			        esql.executeUpdate(enddate_query);
					break;
					
				// EDUCATIONAL DETAILS
				case 6:
					System.out.print("\tEnter Institution Name: ");
					String new_institution = in.readLine();
					
					String inst_query = String.format("UPDATE EDUCATIONAL_DETAILS SET institutionName='%s' WHERE userId='%s'", new_institution, loggedinUser);
			        esql.executeUpdate(inst_query);
			        break;
			    case 7:
					System.out.print("\tEnter Major: ");
					String new_major = in.readLine();
					
					String major_query = String.format("UPDATE EDUCATIONAL_DETAILSSET major='%s' WHERE userId='%s'", new_major, loggedinUser);
			        esql.executeUpdate(major_query);
			        break;
			    case 8:
					System.out.print("\tEnter Degree: ");
					String new_degree = in.readLine();
					
					String degree_query = String.format("UPDATE EDUCATIONAL_DETAILS degree='%s' WHERE userId='%s'", new_degree, loggedinUser);
			        esql.executeUpdate(degree_query);
			        break;
			    case 9:
					System.out.print("\tEnter Start Year (yyyy-mm-dd): ");
					String new_start_year = in.readLine();
					
					String start_query = String.format("UPDATE EDUCATIONAL_DETAILS startDate='%s' WHERE userId='%s'", new_start_year, loggedinUser);
			        esql.executeUpdate(start_query);
			        break;
			    case 10:
					System.out.print("\tEnter End Year (yyyy-mm-dd): ");
					String new_end_year = in.readLine();
					
					String end_query = String.format("UPDATE EDUCATIONAL_DETAILS SET endDate='%s' WHERE userId = '%s'", new_end_year, loggedinUser);
			        esql.executeUpdate(end_query);
					break;
				default: 
					System.out.println("Unrecognized choice!"); 
					break;
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end UpdateProfile

	public static void NewMessage(ProfNetwork esql)
	{
		try{
			System.out.print("Enter user to send message to ('q' to quit): ");
			String receiver = in.readLine();
			if(receiver.equals("q"))
				return;

			//Check whether or not the username the user has entered is valid..
			String validquery = String.format("SELECT userId FROM USR WHERE userId = '%s'", receiver);
			int validqueryvalue = esql.executeQuery(validquery);
			if(validqueryvalue < 1)
			{
				System.out.print("\nThat's an invalid user!\nExit function:NewMessage\n");
				return;
			}

			System.out.print("\tEnter new message ('q' to quit): ");
			String new_mssg = in.readLine();
			if(new_mssg.equals("q")) return;
			
			String messageidquery = String.format("SELECT msgId FROM MESSAGE");
			List<List<String>> queryvalue = esql.executeQueryAndReturnResult(messageidquery);
			int nextid = queryvalue.size() + 1; 	// To get the next mssg id based off max mssg id (kinda)
			
			// DEBUG STUFF
			/*System.out.format("The value of msg id is: %d%n", nextid);
			System.out.format("The value of sender id is: %s%n", loggedinUser);
			System.out.format("The value of receiverId is: %s%n", receiver);
			System.out.format("Contents: %s%n", new_mssg);*/
			
			int deletestats = 0;
			String mssg_status = new String("Delivered");
			String mssg_timestamp = new Timestamp(new Date().getTime()).toString();
			
			String insertmessage = String.format("INSERT INTO MESSAGE(msgId, senderId, receiverId, contents, sendTime, deleteStatus, status) VALUES('%d', '%s', '%s', '%s', '%s', '%d', '%s')", nextid, loggedinUser, receiver, new_mssg, mssg_timestamp, deletestats, mssg_status);
			esql.executeUpdate(insertmessage);				
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end NewMessage


	// Checks if person who gets requested is at most level 3 connections
	public static void checkSendRequest(ProfNetwork esql, String input, String user, int level)
	{
		try
		{
			if(level >= 3)
			{
				System.out.print("User is not at most level 3 connections.\n");
				return;
			}
			String levelquery = String.format("SELECT userId FROM USR WHERE userId = '%s'", user);
								
			List<List<String>> levels = esql.executeQueryAndReturnResult(levelquery);
			for(int i = 0; i < levels.size(); i++)
			{
				//conn_user = level.get(i).get(j);
				user = levels.get(i).get(0);
				if(user.equals(input))	// Found request user
				{
					String addquery = String.format("INSERT INTO CONNECTION_USR(userId, connectionId, status) VALUES('%s', '%s', '%s')", loggedinUser, input, "Accept");
					esql.executeUpdate(addquery);
					System.out.print("\nConnection added!\n");
					return;
				}
				else
				{
					checkSendRequest(esql, input, user, level + 1);
				}
			}
			System.out.print("User is not at most level 3 connections.\n");
			return;
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
	
	public static void SendRequest(ProfNetwork esql)
	{
		try
		{
			System.out.printf("User: %s%n", loggedinUser);
			String query = String.format("SELECT * FROM CONNECTION_USR WHERE UserId = '%s' OR connectionId = '%s'", loggedinUser, loggedinUser);
			List<List<String>> results = esql.executeQueryAndReturnResult(query);
			int size = results.size();
			
			// Person does not have any connections
			if(size == 0)
			{
				System.out.print("This person doesn't have any connections!\n");
				int iterator = 0;
				for (iterator = 0; iterator < 5; iterator++)
				{
					System.out.print("Enter a username that you would like to connect to. (type q to quit.): ");
					String input = in.readLine();
					if(input.equals("q"))
					{
						System.out.print("\nQuitting.\n");
						return;
					}
					else if(input.equals(loggedinUser))
					{
						System.out.print("Can't send connection to yourself.\n");
						return;
					}
					
					String inputquery = String.format("SELECT userId FROM USR WHERE userId = '%s'", input);
					
					int numRows = esql.executeQueryAndPrintResult(inputquery);
					//System.out.printf("Numrows: %s%n", numRows); // debug
					
					if(numRows == 0)
					{
						System.out.print("\nThis user doesn't exist!\n");
						break;
					}
					else 
					{
						System.out.print("Add this user? (y/n): ");
						String addchoice = in.readLine();
						if(addchoice.equals("y") || addchoice.equals("yes"))
						{
							String addquery = String.format("INSERT INTO CONNECTION_USR(userId, connectionId, status) VALUES('%s', '%s', '%s')", input, loggedinUser, "Request");
							esql.executeUpdate(addquery);
							System.out.print("Connection Requested!\n");
						}
					}
				}
			}
			else
			{
				System.out.print("This person has some connections.\n");
				
				System.out.print("Enter a username that you would like to connect to. (type q to quit.): ");
					String input = in.readLine();
					if(input.equals("q"))
					{
						System.out.print("\nQuitting.\n");
						return;
					}
					else if(input.equals(loggedinUser))
					{
						System.out.print("Can't send connection to yourself.\n");
						return;
					}
					String inputquery = String.format("SELECT userId FROM USR WHERE userId = '%s'", input);
					
					int numRows = esql.executeQuery(inputquery);
					//System.out.printf("Numrows: %s%n", numRows); // Debug
					if(numRows == 0)
					{
						System.out.print("This user doesn't exist!\n");
						return;
					}
					else 
					{
						// Now check if input is at most a level 3 connection
						String conn_user = "";
						String curr_user = loggedinUser;
						int found = 0;
						String level_query = String.format("SELECT userId FROM USR WHERE userId = '%s'", curr_user);
							
						List<List<String>> level = esql.executeQueryAndReturnResult(level_query);
						for(int i = 0; i < level.size(); i++)	// To represent levels of connection
						{
							/*String level_query = String.format("SELECT userId FROM USR WHERE UserId = '%s'", curr_user);
							
							List<List<String>> level = esql.executeQueryAndReturnResult(level_query);*/
							//conn_user = "";
							//for(int j = 0; j < level.size(); j++)
							//{
								curr_user = level.get(i).get(0);
								if(curr_user.equals(input))	// Found request user
								{
									//found = 1;
									String addquery = String.format("INSERT INTO CONNECTION_USR(userId, connectionId, status) VALUES('%s', '%s', '%s')", loggedinUser, input, "Accept");
									esql.executeUpdate(addquery);
									System.out.print("\nConnection added!\n");
									return;
								}
								else
								{
									checkSendRequest(esql, input, curr_user, 0);
								}
						}
					}
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end SendRequest

	private static void viewProfileHelper(ProfNetwork esql, String uid) // HELPER
	{
		try
		{
			//System.out.print("made it\n");
			String uid_query = String.format("SELECT status FROM CONNECTION_USR WHERE userId='%s' AND connectionId='%s'", loggedinUser, uid);
			List<List<String>> x = esql.executeQueryAndReturnResult(uid_query);
			
			//System.out.println(x.size());
			int friend_bool = 0;
			//System.out.print("SJEFIWEFJ\n");
			
			if(x.size() > 0)
			{
				String stat = x.get(0).get(0);
				if(stat.equals("Accept")) friend_bool = 1;
				friend_bool = 1;
			}
			
			
			String profile = "\n" + uid + "'s Profile\n";
			System.out.print(profile);
			
			// Get fullname
			String get_query = String.format("SELECT name, dateOfBirth FROM USR WHERE userId='%s'", uid);
			List<List<String>> y = esql.executeQueryAndReturnResult(get_query);
			
			String fullname = y.get(0).get(0); 	// getting name of person
			String date = y.get(0).get(1);		// get date of birth
				
			System.out.print("Name: " + fullname + "\n");
			if(friend_bool == 1) 	// If they are connections, view DOB
			{
				System.out.print("Date of Birth: ");
				System.out.print(date + "\n");
			}
			
			// WORK EXPERIENCE
			System.out.print("\nWork Experience:\n");
			String get_workex_query = String.format("SELECT * FROM WORK_EXPR WHERE userId='%s'", uid);
			
			int workNum = esql.executeQueryAndPrintResult(get_workex_query);
			//System.out.print("\n");
			if(workNum < 1)
				System.out.print("No such Work Experience\n");
			
			// EDUCATIONAL DETAILS
			System.out.print("\nEducational Details:\n");
			String get_edudet_query = String.format("SELECT * FROM EDUCATIONAL_DETAILS WHERE userId='%s'", uid);
			
			int eduNum = esql.executeQueryAndPrintResult(get_edudet_query);
			System.out.print("\n");
			if(eduNum < 1)
				System.out.print("No such Educational Details");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} // end viewProfileHelper

	public static void ViewProfile(ProfNetwork esql)
	{
		try
		{		
			String x = "\n" + loggedinUser + "'s Profile:\n";
			System.out.print(x);
			
			String uid_query = String.format("SELECT name, dateOfBirth FROM USR WHERE userId='%s'", loggedinUser);
			List<List<String>> userlist = esql.executeQueryAndReturnResult(uid_query);
			
			String fullname = userlist.get(0).get(0); 	// getting name of person
			String date = userlist.get(0).get(1);		// getting DOB
			
			System.out.print("Name: " + fullname + "\n");
			System.out.print("Date of Birth: " + date + "\n");
			
			// WORK EXPERIENCE
			System.out.print("Work Experience:\n");
			String get_workex_query = String.format("SELECT * FROM WORK_EXPR WHERE userId='%s'", loggedinUser);
			
			int workNum = esql.executeQueryAndPrintResult(get_workex_query);
			System.out.print("\n");
			if(workNum < 1)
				System.out.print("No such Work Experience");
			
			// EDUCATIONAL DETAILS
			System.out.print("Educational Details:\n");
			String get_edudet_query = String.format("SELECT * FROM EDUCATIONAL_DETAILS WHERE userId='%s'", loggedinUser);
			
			int eduNum = esql.executeQueryAndPrintResult(get_edudet_query);
			System.out.print("\n");
			if(eduNum < 1)
				System.out.print("No such Educational Details");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end ViewProfile


	private static void deleteMenu(ProfNetwork esql, String user_status, String delete_value) // HELPER
	{
		try{
			System.out.print("\nDelete Menu\n");
			System.out.print("0. Don't delete a Message\n");
			System.out.print("1. Delete a Message\n");
			
			int choice = readChoice();
			switch(choice)
			{
				case 0:
					break;
				case 1:
					System.out.print("Enter Message ID of Message you want to delete: ");
					String mssgid = in.readLine();
					
					int new_deletestatus = 0;
					if(user_status.equals("sender"))	// delstatus == 0X
					{
						if(delete_value.equals("0"))
							new_deletestatus = 2;
						else
							new_deletestatus = 3;
					}
					else 			// delstatus == x0
					{
						if(delete_value.equals("0"))
							new_deletestatus = 1;
						else
							new_deletestatus = 3;
					}	
					String delete_query = String.format("UPDATE MESSAGE SET deleteStatus='%s' WHERE msgId='%s'", new_deletestatus, mssgid);
					esql.executeUpdate(delete_query);
					break;
				default:
					System.out.println("Unrecognized choice!"); 
					break;
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}	
	}

	// View Messages and then option to delete messages
	public static void ViewMessages(ProfNetwork esql)
	{
		try{
			System.out.print("\nView Messages:\n");
			System.out.print("-------------\n");
			System.out.print("0. Go back to Menu\n");
			System.out.print("1. View Sent Messages\n");
			System.out.print("2. View Received Messages\n");
			System.out.print("\n");
			
			int choice = readChoice();
			switch (choice)
			{
				case 0:
					break;
				case 1:			// User is sender
					String sent_query = String.format("SELECT * FROM MESSAGE WHERE senderId='%s'", loggedinUser);
					List <List<String>> sent_mssgs = esql.executeQueryAndReturnResult(sent_query);
					
					int sent_size = sent_mssgs.size();
					System.out.print("Messages Sent: \n");
					System.out.print("\nMessage ID\tTo\t\tContents\t\t\tSend Time");
					
					String col_string = "";
					String col_temp = "";
					String delete_value1 = "0";
					for(int row = 0; row < sent_size; row++)
					{
						col_string = "";
						for(int col = 0; col < sent_mssgs.get(row).size(); col++)
						{
							int delete_stat = 0;
							if(col == 5) 	// check is Mssg is 'deleted'
							{
								delete_value1 = sent_mssgs.get(row).get(col);
								if (delete_value1.equals("2") || delete_value1.equals("3"))
								{
									delete_stat = 1; // Mssg has deleteStatus == 1X
									col_string = "";
								}
							}
							
							if(delete_stat != 1)
							{
								if(col != 1 && col != 5 && col != 6)
								{
									col_temp = sent_mssgs.get(row).get(col);
									col_string = col_string + col_temp + " ";
								}
							}
							else break;
						}
						// Print Message after loop
						if(col_string.length() > 0)
							System.out.print("\n" + col_string);
						//System.out.print("\n");
					}
					System.out.print("\n");
					deleteMenu(esql, "sender", delete_value1);
					break;
				case 2:		// User is receiver
					String receive_query = String.format("SELECT * FROM MESSAGE WHERE receiverId='%s'", loggedinUser);
					List <List<String>> received_mssgs = esql.executeQueryAndReturnResult(receive_query);
					
					int size = received_mssgs.size();
					System.out.print("Messages: \n");
					System.out.print("\nMessage ID\tFrom\t\tContents\t\t\tSend Time\n");
					
					String col_string2 = "";
					String col_temp2 = "";
					String delete_value2 = "0";
					for(int row = 0; row < size; row++)
					{
						col_string2 = "";
						for(int col = 0; col < received_mssgs.get(row).size(); col++)
						{
							int delete_stat = 0;
							if(col == 5) 	// check is Mssg is 'deleted'
							{
								delete_value2 = received_mssgs.get(row).get(col);
								if (delete_value2.equals("1") || delete_value2.equals("3"))
								{
									delete_stat = 1; // Mssg has deleteStatus == 1X
									col_string2 = "";
								}
							}
							if(delete_stat != 1)
							{
								if(col != 2 && col != 5 && col != 6)
								{
									col_temp2 = received_mssgs.get(row).get(col);
									col_string2 = col_string2 + col_temp2 + " ";
								}
							}
							else break;
						}
						// Print Message after loop
						if(col_string2.length() != 0)
							System.out.print("\n" + col_string2);
						//System.out.print("\n");
					}
					System.out.print("\n");
					deleteMenu(esql, "receiver", delete_value2);
					break;
				default:
					System.out.println("Unrecognized choice!"); 
					break;
			}
			
			/*for(List<String> row : received_mssgs)
			{
				System.out.print("From\t\tContents\tSend Time\n");
				for(String col : row)
				{
					//if(row.get())
						//break;
					System.out.print(col + " ");
				}
				System.out.print("\n");
			}*/
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end ViewMessages

	public static void ChangePassword(ProfNetwork esql)
	{
		try{
			System.out.print("\tEnter new password ('q' to quit): ");
			String newPassword = in.readLine();
			if(newPassword.equals("q")) 
				return;
			String query = String.format("UPDATE USR SET password='%s' WHERE userId='%s'", newPassword, loggedinUser);
			esql.executeUpdate(query);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end ChangePassword

	// Search by Full name
	public static void SearchPeople(ProfNetwork esql)
	{
		try{
			System.out.print("\tEnter person to search ('q' to quit): ");
			String person = in.readLine();
			if(person.equals("q")) return;
			
			String query = String.format("SELECT name, userId, email FROM USR WHERE name='%s'", person);
			int searchNum = esql.executeQueryAndPrintResult(query);
			if(searchNum < 1)
			{
				System.out.print("No User, " + person + " exists.\n");
				return;
			}
			
			System.out.print("\n");
			// Option to send friend request
			
			System.out.print("Options\n");
			System.out.print("-------\n");
			System.out.print("0. Go back to Menu\n");
			System.out.print("1. View Profile of Searched people\n");
			//System.out.print("2. Send Requesto to searched people\n");
			
			int choice = readChoice();
			switch(choice)
			{
				case 0: return;
				case 1:		// View EXISTING user's profile
					System.out.print("Enter userId of a searched person: ");
					String view_userId = in.readLine();
					
					//String view_query = String.format("SELECT ", view_response);
					viewProfileHelper(esql, view_userId);
					break;
				default:
					System.out.println("Unrecognized choice!");
					break;
			}
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end SearchPeople

	public static void ViewRequests(ProfNetwork esql)
	{
		try{
			System.out.print("\n\tView Requests\n");
			System.out.print("\t-------------\n");
			System.out.print("\t0. Go back to Menu\n");
			System.out.print("\t1. Print requests\n");
			System.out.print("\t2. Accept requests\n");
			System.out.print("\t3. Reject requests\n");
			
			int choice = readChoice();
			switch(choice)
			{
				case 0:
					break;
				case 1:
					String request_query = String.format("SELECT * FROM CONNECTION_USR WHERE userId='%s' AND status='Request'", loggedinUser);
					List<List<String>> requests = esql.executeQueryAndReturnResult(request_query);
					
					int requests_size = requests.size();
					String col_element = "";
					System.out.print("\tConnection Id\tStatus");
					for(int row = 0; row < requests_size; row++)
					{
						col_element = "";
						for(int col = 0; col < requests.get(row).size(); col++)
						{
							if(col != 0)
							{
								String col_elem_temp = requests.get(row).get(col);
								col_element = col_element + "\t" + col_elem_temp + " ";
							}
						}
						System.out.print("\n" + col_element);
					}
					break;
				case 2:
					System.out.print("Enter Connection Id to accept: ");
					String accept_id = in.readLine();
					
					String accept_query = String.format("UPDATE CONNECTION_USR SET status='Accept' WHERE userId='%s' AND connectionId='%s' AND status='Request'", loggedinUser, accept_id);
					esql.executeUpdate(accept_query);
					System.out.print("\nRequest Accepted!\n");
					break;
				case 3:
					System.out.print("Enter Connection Id to reject: ");
					String reject_id = in.readLine();
					
					String reject_query = String.format("UPDATE CONNECTION_USR SET status='Reject' WHERE userId='%s' AND connectionId='%s' AND status='Request'", loggedinUser, reject_id);
					esql.executeUpdate(reject_query);
					System.out.print("\nRequest Rejected!\n");
					break;
				default:
					System.out.println("Unrecognized choice!"); 
					break;
			}
			System.out.print("\n");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	} //end ViewRequests

}//end ProfNetwork
