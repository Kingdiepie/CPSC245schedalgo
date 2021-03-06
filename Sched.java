/**CAN HANDLE TIME – BONUS
 * Dominik Yakoubek
 * CPSC245 Prog#2 Sched Algorithms
 * 10/30/2018
 * This program takes a file oath a a command line argument 
 * and will read that data to get information about processes.
 * Useing that data average waiting time and average turnaround
 * time is calculated using FCFS, SJF nad priority algorithms.
 *
 * Exit Codes:
 * 0: Correctly Executed
 * 1: Invalid amount on command line arguments
 * 2: File not found
 * 3: File is Empty
 * 4: Sementic Error doing contact switch
**/

import java.util.*;
import java.math.*;
import java.io.*;
import java.lang.*;

public class Sched
{

    public static void main(String args[])
    {
	id();

	//Check for valid # cmd arguments
	if(args.length != 1)
	{
	    System.err.println("ERROR: invalid command line argurments.");
	    System.err.println("Please Specifiy a data file.");
	    System.exit(1);
	}

	//Load processes into array
	ArrayList<Process> processes = loadProcesses(args[0]);
	//Run calculation with each algorithm
	calculateWith("FCFS",processes);
	calculateWith("SJF",processes);
	calculateWith("PRI",processes);
	id();
	System.exit(0);
    }


    //Unchanged ID message, is there a reason this is public?
    public static void id()
    {
	System.out.println("Dominik Yakoubek: Scheduling Algo assignment");
    } 
 

    //Method for loading processes into arraylist
    private static ArrayList<Process> loadProcesses(String in_file)
    {
	ArrayList<Process> inputProcesses = new ArrayList<Process>();
	int lines = 0;
	
        try
	{   //Attempt to load file
            Scanner in = new Scanner(new File(in_file));

	    //add processes from file into arraylist
            while(in.hasNext()){
		lines++;
                inputProcesses.add(new Process(in.next(), in.nextDouble(), in.nextInt() ,in.nextDouble()));
            }

        }
        catch(FileNotFoundException e)
        {
            System.err.println("ERROR: file " + in_file + " not found");
            System.exit(2);
        }

	//Make sure file is not empty
	if(inputProcesses.size() == 0)
	{
	    System.err.println("ERROR: File is empty");
	    System.exit(3);
	}

	System.out.printf("Read %d lines from file '%s'.\n", lines, in_file);
	return inputProcesses;
    }
    

    //Sort for FCFS algorithm
    private static ArrayList<Process> sortFCFS(ArrayList<Process> processes)
    {
	//selection sort based on time until arrival
	for (int i = 0; i < processes.size(); i++)
	{
	    int min = i;

	    for (int j = i; j < processes.size(); j++)
	    {
		if (processes.get(j).getTimeUntilArvl() < processes.get(min).getTimeUntilArvl())
		    min = j;
	    }

	    Process temp = processes.get(min);
	    processes.set(min, processes.get(i));
	    processes.set(i, temp);
        }

	return processes;
    }

    
    //Sort for Priority algorithm
    private static ArrayList<Process> sortPRI(ArrayList<Process> processes)
    {
	//selection sort based on priority
	for (int i = 0; i < processes.size(); i++)
        {
	    int min = i;

            for (int j = i; j < processes.size(); j++)
	    {
		if (processes.get(j).getPriority() < processes.get(min).getPriority())
		    min = j;
            }
	    
                Process temp = processes.get(min);
                processes.set(min, processes.get(i));
                processes.set(i, temp);
        }

	return processes;
    }

    
    //Calculation method:
    private static void calculateWith(String algrthm, ArrayList<Process> processes)
    {

	//Check for any needed sorting
	if(algrthm.equals("FCFS"))
	    processes = sortFCFS(processes);
	else if(algrthm.equals("PRI"))
	    processes = sortPRI(processes);

	double[] indexAndTime;
	double awt = 0;
	double att = 0;
   
	//Simulate processes running
	while(jobsToDo(processes))
	{
	    //Find next process to run for a certain time
	    indexAndTime = findProcessToRun(algrthm, processes);
	    //update times of processes
	    processes = updateTimes(indexAndTime, processes);
	}

	//Calculate att and awt
	for(Process p : processes)
	{
	    att += p.getTrnArndTm();
	    awt += p.getWaitnTm();
	    //reset process since work with it is done
	    p.reset();
	}
	
	awt /= processes.size();
	att /= processes.size();
	display_result(algrthm, awt, att);
    }
    

    //Check to see if any processes still need to be executed
    private static boolean jobsToDo(ArrayList<Process> processes)
    {
	for(Process p : processes)
	    if(p.getDurationLeft() > 0)
		return true;

	return false;
    }
    
    //Method to determine which process to run for how long, or how long to wait idle. 
    private static double[] findProcessToRun(String algrthm, ArrayList<Process> processes)
    {	
	double tmUntlArvl;
        double duration;
	double tmUntilFrstArvl;

	//Use correct algorithm
	switch(algrthm)
        {
	case "FCFS":

	    //loop through sorted processes
	    for(int i = 0; i < processes.size(); i++)
            {
		tmUntlArvl = processes.get(i).getTimeUntilArvl();
		duration = processes.get(i).getDurationLeft();
		//If Process still needs work and has arrived run it
		if(duration > 0 && tmUntlArvl <= 0)
		{
		    return new double[]{i,duration};             
		}
		//If Process has work, but has not arrived, wait until it does
		else if (duration != 0){
		    return new double[]{-1,processes.get(i).getTimeUntilArvl()};
		} 
	    }
	    
        case "SJF":

	    double time = Double.MAX_VALUE;
	    tmUntilFrstArvl = Double.MAX_VALUE;
	    int processToRunIdx = -1;

            for(int i = 0; i < processes.size(); i++)
            {
                duration = processes.get(i).getDurationLeft();
                tmUntlArvl = processes.get(i).getTimeUntilArvl();
		//See if current process has the shortest time until arrival
		if(tmUntlArvl != 0 && tmUntlArvl< tmUntilFrstArvl)  
		{
                    tmUntilFrstArvl = tmUntlArvl;
                }
		//If a process is ready to run and is shortest job save it
                else if (tmUntlArvl == 0 && duration < time && duration != 0)
                {
                    time = duration;
                    processToRunIdx = i;
		    tmUntilFrstArvl = 0;
                }
            }

	    //If no process is ready to run wait until one is
	    if(tmUntilFrstArvl > 0)
		return new double[]{-1, tmUntilFrstArvl};

	    //Run saved process
	    return new double[]{processToRunIdx, time};
	    
	case "PRI":

	    tmUntilFrstArvl = Double.MAX_VALUE;
                    
	    for(int i = 0; i < processes.size(); i++)
            {
		tmUntlArvl = processes.get(i).getTimeUntilArvl();
                duration = processes.get(i).getDurationLeft();
		//If highest prioty job has work, and is ready, run it 
		if(duration > 0 && tmUntlArvl == 0)
                {
                    return new double[]{i,duration};
                }
		//Otherwise, see if this process has the shortest time until arrival
		else if(tmUntlArvl < tmUntilFrstArvl)
		{
		    tmUntilFrstArvl = tmUntlArvl;
		}
            }

	    //Wait the duration of the shortest time until arrivl
	    return new double[]{-1,tmUntilFrstArvl};
        }

	//We should never get here
	System.err.println("ERROR: Fail to perform a contact switch.");
	System.exit(4);
	return null;
    }
    
    //Method used to tell a process to run or wait
    private static ArrayList<Process> updateTimes(double[] idxTime, ArrayList<Process> processes)
    {
      	for(int i = 0; i < processes.size();i++)
	{
	    //See if current process needs to run
	    if(i == idxTime[0])
		processes.get(i).run(idxTime[1]);
	    //If current process does not need to run then wait.
	    else if(processes.get(i).getDurationLeft()>0)
		processes.get(i).wait(idxTime[1]);
	}

	return processes;
    }

    
    private static void display_result(String label, double awt, double att)
    //label - pri/fcfc/sjf only
    //awt - average waiting time
    //att - average turnaround time
    {
	label = label.toUpperCase();
	boolean label_ok = label.equals("PRI") || label.equals("FCFS") || label.equals("SJF");

	if(label_ok)
	{
	    System.out.println(label);
	    System.out.println("==============================");
	    System.out.printf("Avg Waiting Time (%s): %6.2f\n", label, awt);
	    System.out.printf("Avg TurnArd Time (%s): %6.2f\n\n", label, att);
	}
    }
    

}

class Process
{
    private int priority;
    private double duration, durationLeft, arvlTm, waitnTm, trnArndTm, timeUntilArvl;
    private String processNm;
    
    public Process(String processNm, double duration, int priority, double arvlTime)
    {
	this.duration = this.durationLeft = duration;
	this.priority = priority;
	this.arvlTm = timeUntilArvl = arvlTime;
	this.waitnTm = 0;
	this.trnArndTm = 0;
	this.processNm = processNm;
    }

    
    //Reset fields
    public void reset()
    {
	this.durationLeft = this.duration;
	this.timeUntilArvl = this.arvlTm;
	this.waitnTm = 0;
    	this.trnArndTm = 0;
    }
    
    //Simulate waiting
    public void wait(double time)
    {
	//handle reduceing time until arrival
	if(this.timeUntilArvl > 0)
	{
	    this.timeUntilArvl -= time;

	    //handle waiting longer then time until arrival
	    if(this.timeUntilArvl < 0)
	    {
		    this.waitnTm -= this.timeUntilArvl;
		    this.trnArndTm -= this.timeUntilArvl;	    
		    this.timeUntilArvl = 0;
	    }
	    
	}
	else
	{
	    this.waitnTm += time;
	    this.trnArndTm += time;
	}
	
    }


    //update fields based on time to run. 
    public void run(double time)
    {
	this.durationLeft -= time;
	this.trnArndTm += time;
    }
    

    public int getPriority()
    {
	return this.priority;
    }

    
    public double getDurationLeft()
    {
	return this.durationLeft;
    }

    
    public double getWaitnTm()
    {
	return this.waitnTm;
    }

    
    public double getTimeUntilArvl()
    {
	return this.timeUntilArvl;
    }

    
    public double getTrnArndTm()
    {
	return this.trnArndTm;
    }

 
}
