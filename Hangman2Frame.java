/* This program displays a game of Hangman using a Graphical User Interface.
This game uses defulary words and displays its definition as a clue.
The vocabulary and definitions are stored in a text file called WordsAndDefs.txt
and was taken from Top 100 SAT Words on vocabulary.com
https://www.vocabulary.com/lists/23400

The JPanel and JFrame code structure was adapted from
various code from my Intro to Java Course
This program runs the main method in Hangman2Frame class.

Created in 2017 and edited in 2020
By Ethan Chen
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
public class Hangman2Frame extends JFrame{
	public static void main (String [] args){		//Runs main method here
		Hangman2Frame c = new Hangman2Frame();
	}
	//sets up JPanel frame
	public Hangman2Frame(){ 
		super ("Hangman2");
		setSize(1500,1000);
		setLocation(0,0);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Hangman2 p = new Hangman2();
		setContentPane(p);
		setVisible(true);
	}
}

class Hangman2 extends JPanel implements KeyListener, FocusListener, MouseListener{ 
	private enum GameStatus{PLAYING, WIN, LOSE};
	private GameStatus status;
	private String [] answer = new String [6];
	private ArrayList<String> vocab;
	private ArrayList<String> definitions;
	private String wrong;			//string of incorrect letters
	private boolean [] correct;		//determines which characters have been selected
	private boolean isFocused;		//determines if the window is clicked on
	private int [][] colors = {
		{0, 2, 2},		//red
		{0, 1, 2},		//orange
		{0, 0, 2},		//yellow
		{2, 0, 2},		//green
		{2, 0, 1},		//aqua green
		{2, 1, 0},		//light blue
		{2, 2, 0},		//dark blue
		{1, 2, 0},		//purple
		{0, 2, 0},		//magenta
		{1, 1, 1},		//gray
	}; 
	private int hangNum;
	private int ansNum;
	private int score;
	private int backNum; //integer that determines background color
	public Hangman2(){
		addMouseListener(this); //add mouse/key/focus listener
		addKeyListener(this);
		addFocusListener(this);
		Color backColor = Color.GRAY;
		setBackground(backColor);
		isFocused = false;
		score = 0;
		readFile();
		setUp();
	}
	//reads text file with words and definitions
	public void readFile() {
		vocab = new ArrayList<String>();
		definitions = new ArrayList<String>();
		try {
			File f = new File("WordsAndDefs.txt");
			Scanner scnr = new Scanner(f);
			int count = 0;
			while(scnr.hasNextLine()) {
				if(count == 0) {
					vocab.add(scnr.nextLine().toUpperCase());
				}else {
					definitions.add(scnr.nextLine());
				}
				count = (count+1) % 2;
			}
		}catch(FileNotFoundException e) {
			System.out.println("WordsAndDefs.txt could not be found");
			System.exit(1);
		}
	}
	//sets up the initial state of the Hangman Board
	public void setUp(){
		wrong = "";
		status = GameStatus.PLAYING;
		hangNum = 0;		//how many letters wrong for that turn
		backNum = (int)(Math.random()*10);	//chooses the color of the background
		getAnswer();
		correct = new boolean [answer.length];
		for(int i=0; i<correct.length; i++)
		{
			correct[i] = false;
		}
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g); //draws background
		Font listF  = new Font ( "Helvetica", Font.BOLD, 60 );
		g.setFont ( listF );
		if(isFocused) {
			drawBackground(g);
			run(g);
		}else {
			drawPaused(g);
		}
	}
	//prints the paused screen when the window has lost focus
	public void drawPaused(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font ( "Helvetica", Font.BOLD, 100 ));
		g.drawString("Hangman Vocabulary", 200, 400);
		g.setFont(new Font ( "Helvetica", Font.BOLD, 60 ));
		g.drawString("Click the window to continue", 200, 550);
	}
	//Chooses the word that will be the answer to the current hangman game
	public void getAnswer(){
		ansNum = (int)(Math.random()*vocab.size());
		String ansA = vocab.get(ansNum);
		String [] tempArray = new String [ansA.length()];
		for(int i=0; i<ansA.length(); i++){ //stores answer in an array
			tempArray[i] = (ansA.charAt(i) + "");
			answer = tempArray;
		}
	}
	public void drawBackground(Graphics g){
		//draw Sky
		int red, green, blue;
		for(int i=50; i<255; i++){
			if(status == GameStatus.LOSE){
				red = ((2-colors[backNum][0]) * (255-i))/2;
				green = ((2-colors[backNum][1]) * (255-i))/2;
				blue = ((2-colors[backNum][2]) * (255-i))/2;
			}else{
				red = 255 - (colors[backNum][0] * i)/2;
				green = 255 - (colors[backNum][1] * i)/2;
				blue = 255 - (colors[backNum][2] * i)/2;
			}
			g.setColor(new Color(red, green, blue));
			g.fillRect(0, 1200-i*5, 1500, 5);
		}
		//draw Stand
		Color standColor = new Color(0, 0, 0);
		g.setColor(standColor);
		g.fillRect(0, 600, 1500, 490);
		int x = 150;//position
		int y = 50;
		int sizeX = 10;//size
		int sizeY = 10;
		g.fillRect(x, y, sizeX*30, sizeY*2);
		g.fillRect(x, y, sizeX*2, sizeY*55);
		g.fillRect(x+sizeX*30, y, sizeX*2, sizeY*10);
		int [] a = {x-sizeX*5, x+sizeX, x+sizeX*7};
		int [] b = {y+sizeY*55, y+sizeY*52, y+sizeY*55};
		g.fillPolygon(a, b, 3);
		g.setColor(Color.WHITE);
		g.setFont(new Font ( "Helvetica", Font.BOLD, 40 ));
		g.drawString("Definition: ", 100, 810);
		g.drawString(definitions.get(ansNum), 100, 860);
		g.setFont(new Font ( "Helvetica", Font.BOLD, 60 ));
	}
	public void run(Graphics g){
		printInput(g);
		drawHangman(g);
		switch(status) {
			case PLAYING:
				printPlaying(g);
				break;
			case WIN:
				printWin(g);
				break;
			case LOSE:
				printLose(g);
				break;
		}
	}
	public void printInput(Graphics g){
		//print correct input
		for(int i=0; i<answer.length; i++){
			g.setColor(Color.WHITE);
			g.fillRect(50+i*80, 720, 50, 5);
			if(correct[i] == true)
				g.drawString(answer[i], 50+i*80, 700);
			else if(status == GameStatus.LOSE){
				g.setColor(Color.RED);
				g.drawString(answer[i], 50+i*80, 700);
			}
		}
		//print wrong input
		g.setColor(Color.BLACK);
		g.drawString(wrong, 600, 100);
		//print score
		g.drawString("Score:", 1000, 100);
		g.drawString((score+""), 1200, 100);
	}
	public void drawHangman(Graphics g){
		if(hangNum >= 6)
			g.setColor(Color.RED);
		else
			g.setColor(Color.BLACK);
		hangNum = wrong.length();
		if(hangNum >= 1)
			g.fillOval(415, 150, 90, 90);
		if(hangNum >= 2)
			g.fillRect(410, 250, 100, 150);
		if(hangNum >= 3)
			g.fillRect(410, 400, 30, 150);
		if(hangNum >= 4)
			g.fillRect(480, 400, 30, 150);
		if(hangNum >= 5)
			g.fillRect(370, 250, 30, 150);
		if(hangNum >= 6){
			status = GameStatus.LOSE;
			g.fillRect(520, 250, 30, 150);
		}
	}
	public void printPlaying(Graphics g) {
		Font endF = new Font("Helvetica", Font.BOLD, 30);
		g.setFont(endF);
		g.setColor(Color.BLACK);
		g.drawString("Type in letters to guess the word", 850, 200);
		g.drawString("Or press SPACE for a new word", 850, 250);
	}
	public void printWin(Graphics g) {
		Font endF = new Font("Helvetica", Font.BOLD, 100);
		g.setFont(endF);
		g.setColor(Color.WHITE);
		g.drawString("Congratulations!", 650, 300);
		end(g);
	}
	public void printLose(Graphics g) {
		g.setColor(Color.RED);
		Font endF = new Font("Helvetica", Font.BOLD, 100);
		g.setFont(endF);
		g.drawString("H A N G M A N!", 650, 300);
		end(g);
	}
	public void checkIfWon(){
		for(int i=0; i<correct.length; i++){
			if(!correct[i]){
				return;
			}
		}
		status = GameStatus.WIN;
		score++;
	}
	public void end(Graphics g){
		Font endD = new Font("Helvetica", Font.BOLD, 70);
		g.setFont(endD);
		g.drawString("Press SPACE to restart", 650, 450);
		repaint();
	}
	public void mousePressed(MouseEvent e){
		requestFocus();
	}
	public void mouseReleased(MouseEvent e){
	}
	public void mouseClicked(MouseEvent e){
	}
	public void mouseEntered(MouseEvent e){
	}
	public void mouseExited(MouseEvent e){
	}
	public void keyTyped(KeyEvent e){
		String input = "";
		char letter = e.getKeyChar();
		boolean wrongA = true;

		if(status == GameStatus.PLAYING && !(letter == ' ' || (int)e.getKeyChar() == 10)){
			input = "" + letter;
			input = input.toUpperCase();
			for(int i=0; i<answer.length; i++)
			{
				if(input.equals(answer[i])){
					correct[i] = true;
					wrongA = false;
				}
			}

			//adds the wrong letter to the list
			if(wrongA && wrong.indexOf(input) == -1){
				wrong += input;
			}
			if(!wrongA) {
				checkIfWon();
			}
			repaint();
		}
	}
	public void keyPressed(KeyEvent e){
		int value = e.getKeyCode();
		if (value == KeyEvent.VK_SPACE)   {
			setUp( );
			repaint();
		}
	}
	public void keyReleased(KeyEvent e){
	}
	public void focusGained(FocusEvent e){
		isFocused = true;
		repaint();
	}
	public void focusLost(FocusEvent e){
		isFocused = false;
		repaint();
	}
}