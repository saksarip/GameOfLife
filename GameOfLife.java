import javalib.funworld.*;
import javalib.worldimages.*;
import java.awt.Color;
import tester.*;

import java.util.Random;


class GameOfLife extends World{
  static final int CELL_SIZE = 20;
  CellMatrix board;
  double prob;
  boolean paused;
  double tickRate;
  int tickCount = 0;
  GameOfLife(CellMatrix board, double prob, boolean paused, double tickRate, boolean isInitState) {
    if(prob > 1) {
      throw new IllegalArgumentException("Prob must be less than or equal to 1");
    }
    this.prob = prob;
    if(isInitState) {
      this.board = board.randomize(prob);
    }
    else {
      this.board = board;
    }
    this.paused = paused;
    this.tickRate = tickRate;
  }
  
  GameOfLife(CellMatrix board, double prob, double tickRate) {
    this(board, prob, false, tickRate, true);
  }
  
  GameOfLife updateBoard() {
    CellMatrix outputMatrix = new CellMatrix(this.board.getCells());
    for(int row = 1; row < board.getCells().length - 1; row++) {
      for(int col = 1; col < board.getCells()[0].length - 1; col++) {
        outputMatrix.getCells()[row][col] = 
            this.board.getCells()[row][col].updateCell(this.board, row, col);
      }
    }
    return new GameOfLife(outputMatrix, this.prob, this.paused, this.tickRate, false);
  }
  
  public GameOfLife onTick() {
    tickCount++;
    if(tickCount % this.tickRate == 0) {
      if(this.paused) {
        return this;
      }
      else {
        return this.updateBoard();  
      }
    }
    else {
      return this;
    }
    
  }
  
  String paused() {
    if(this.paused) {
      return "PAUSED";
    }
    else {
      return "";
    }
  }
  
  public GameOfLife onKeyReleased(String key) {
    if(key.equals("r")) {
      System.out.println(this.prob + " probability");
      return new GameOfLife(new CellMatrix(this.board.getCells().length, 
          this.board.getCells()[0].length), this.prob, this.paused, this.tickRate, true);
    }
    if(key.equals(" ")) {
      return new GameOfLife(this.board, this.prob, !this.paused, this.tickRate, false);
    }
    if(key.equals("s")) {
      return new GameOfLife(this.board, this.prob, this.paused, Math.min(this.tickRate + 1, 5), false);
    }
    if(key.equals("f")) {  
      return new GameOfLife(this.board, this.prob, this.paused, Math.max(this.tickRate -1 , 1), false);
    }
    else return this;
  }
  
  public WorldScene makeScene() {
    return new WorldScene((board.getCells().length * CELL_SIZE) + 20, 
        board.getCells()[0].length * CELL_SIZE).placeImageXY(board.drawCells(), 
            (board.getCells().length * CELL_SIZE / 2) - CELL_SIZE,
            (board.getCells()[0].length * CELL_SIZE / 2) + ((CELL_SIZE - 20) * -1)).placeImageXY(
                new RectangleImage(board.getCells()[0].length * CELL_SIZE, 20, OutlineMode.SOLID, Color.black), 
                (board.getCells()[0].length * CELL_SIZE / 2) - (CELL_SIZE * 2), 10).placeImageXY(
                new TextImage("Speed of Simulation: " + 
                Double.toString(6 - this.tickRate), 10, Color.red), 
                55, 7).placeImageXY(new TextImage(this.paused(), 10, Color.red), 170, 7).placeImageXY(
                    new TextImage("Press:  F/S Increase/Decrease Speed  R Reset Board  "
                    + "SPACE Pause Board", 10, Color.red),
                    170, 17);
  }
}

class CellMatrix {
  Cell[][] cells;
  
  CellMatrix(int length, int width) {
    this.cells = new Cell[length+2][width+2];
    for(int row = 0; row < length; row++) {
      for(int col = 0; col < width; col++) {
        this.cells[row][col] = new Cell(false);
      }
    }
  }
  
  CellMatrix(Cell[][] cells) {
    this.cells = cells;
  }
  
  CellMatrix randomize(double prob) {
    for(int row = 0; row < this.cells.length; row++) {
      for(int col = 0; col < this.cells[0].length; col++) {
        if(row > 0 && row < this.cells.length - 1 && col > 0 && col < this.cells[0].length - 1) {
          if(Math.random() < prob) {
            this.cells[row][col] = new Cell(true);
          }
          else {
            this.cells[row][col] = new Cell(false);
          }
        }
        else {
          this.cells[row][col] = new Cell(false);
        }
      }
    }
    return new CellMatrix(this.cells);
  }
  
  Cell[][] getCells() {
    return cells;
  }
  
  WorldImage drawCells() {
    WorldImage row = new EmptyImage();
    WorldImage grid = new EmptyImage();
    for(int rows = 1; rows < cells.length - 1; rows++) {
      for(int col = 1; col < cells[0].length - 1; col++) {
        row = new BesideImage(row, cells[rows][col].drawCell());
      }
      grid = new AboveImage(grid, row);
      row = new EmptyImage();
    }
    return grid;
  }
  
}

class Cell {
  boolean isAlive;

  Cell(boolean isAlive) {
    this.isAlive = isAlive;
  }
  
  Cell updateCell(CellMatrix cells, int row, int col) {
    Cell[] surroundingCells = {cells.getCells()[row-1][col-1], cells.getCells()[row-1][col],
        cells.getCells()[row-1][col+1], cells.getCells()[row][col-1], cells.getCells()[row][col],
        cells.getCells()[row][col+1], cells.getCells()[row+1][col-1], cells.getCells()[row+1][col],
        cells.getCells()[row+1][col+1]};
    int numAlive = 0;
    for(Cell cell : surroundingCells) {
      if(cell.isAlive) {
        numAlive++;
      }
    }
    if(cells.getCells()[row][col].isAlive) {
      return new Cell(numAlive == 3 || numAlive == 4);
    }
    else {
      return new Cell(numAlive == 3);
    }
    
  }

  WorldImage drawCell() {
    if (this.isAlive) {
      return new RectangleImage(GameOfLife.CELL_SIZE, GameOfLife.CELL_SIZE, 
          OutlineMode.SOLID, Color.green);
    } else {
      return new RectangleImage(GameOfLife.CELL_SIZE, GameOfLife.CELL_SIZE, 
          OutlineMode.SOLID, Color.black);
    }
  }
}


//Game Examples Class
class ExamplesGameOfLife {
  CellMatrix cells = new CellMatrix(40, 40);
  boolean testBigBang(Tester t) {
    GameOfLife gm = new GameOfLife(cells, .1, 1);
    gm.bigBang(cells.getCells().length * GameOfLife.CELL_SIZE + 20, 
        cells.getCells()[0].length * GameOfLife.CELL_SIZE, .01);
    return true;
  }
  
  
  
}
