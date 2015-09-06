package com.example.draganglumac.noughtsandcrosses;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.HashSet;
import java.util.Set;

public class GameFragment extends Fragment {

    static private int largeIds[] = {R.id.large1, R.id.large2, R.id.large3, R.id.large4,
            R.id.large5, R.id.large6, R.id.large7, R.id.large8, R.id.large9};
    static private int smallIds[] = {R.id.small1, R.id.small2, R.id.small3, R.id.small4,
            R.id.small5, R.id.small6, R.id.small7, R.id.small8, R.id.small9};

    private Tile entireBoard = new Tile(this);
    private Tile largeTiles[] = new Tile[9];
    private Tile smallTiles[][] = new Tile[9][9];
    private Tile.Owner player = Tile.Owner.X;
    private Set<Tile> available = new HashSet<>();
    private int lastLarge;
    private int lastSmall;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes,
        // e.g. when parent Activity gets destroyed on rotation.
        setRetainInstance(true);
        initGame();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.large_board, container, false);
        initViews(rootView);
        updateAllTiles();
        return rootView;
    }

    public void initGame() {
        Log.d("UNC3", "init game");
        entireBoard = new Tile(this);
        // Create all the tiles
        for (int large = 0; large < 9; large++) {
            largeTiles[large] = new Tile(this);
            for (int small = 0; small < 9; small++) {
                smallTiles[large][small] = new Tile(this);
            }
            largeTiles[large].setSubTiles(smallTiles[large]);
        }
        entireBoard.setSubTiles(largeTiles);

        // If the player moves first, set which spots are available
        lastSmall = -1;
        lastLarge = -1;
        setAvailableFromLastMove(lastSmall);
    }

    private void initViews(View rootView) {
        entireBoard.setView(rootView);
        for (int large = 0; large < 9; large++) {
            View outer = rootView.findViewById(largeIds[large]);
            largeTiles[large].setView(outer);

            for (int small = 0; small < 9; small++) {
                ImageButton inner = (ImageButton) outer.findViewById(smallIds[small]);
                final int largeCopy = large;
                final int smallCopy = small;
                final Tile smallTile = smallTiles[large][small];
                smallTile.setView(inner);
                inner.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (isAvailable(smallTile)) {
                            makeMove(largeCopy, smallCopy);
                            switchTurns();
                        }
                    }
                });
            }
        }
    }

    private void makeMove(int large, int small) {
        lastLarge = large;
        lastSmall = small;
        Tile smallTile = smallTiles[large][small];
        Tile largeTile = largeTiles[large];

        smallTile.setOwner(player);
        setAvailableFromLastMove(small);

        Tile.Owner oldWinner = largeTile.getOwner();
        Tile.Owner winner = largeTile.findWinner();
        if (winner != oldWinner)
            largeTile.setOwner(winner);

        winner = entireBoard.findWinner();
        entireBoard.setOwner(winner);
        updateAllTiles();
        if (winner != Tile.Owner.NEITHER)
            ((GameActivity) getActivity()).reportWinner(winner);
    }

    private void switchTurns() {
        player = (player == Tile.Owner.X) ? Tile.Owner.O : Tile.Owner.X;
    }

    public void restartGame() {
        initGame();
        initViews(getView());
        updateAllTiles();
    }

    private void clearAvailable() {
        available.clear();
    }

    private void addAvailable(Tile tile) {
        available.add(tile);
    }

    public boolean isAvailable(Tile tile) {
        return available.contains(tile);
    }

    private void setAvailableFromLastMove(int small) {
        clearAvailable();

        // Make all the tiles at the destination available
        if (small != -1) {
            for (int dest = 0; dest < 9; dest++) {
                Tile tile = smallTiles[small][dest];
                if (tile.getOwner() == Tile.Owner.NEITHER) {
                    addAvailable(tile);
                }
            }
        }

        // If there were none available, make all squares available
        if (available.isEmpty())
            setAllAvailable();
    }

    private void setAllAvailable() {
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile tile = smallTiles[large][small];
                if (tile.getOwner() == Tile.Owner.NEITHER)
                    addAvailable(tile);
            }
        }
    }

    /**
     * Create a string containing the state of the game.
     */
    public String getState() {
        StringBuilder builder = new StringBuilder();
        builder.append(lastLarge);
        builder.append(',');
        builder.append(lastSmall);
        builder.append(',');
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                builder.append(smallTiles[large][small].getOwner().name());
                builder.append(',');
            }
        }
        return builder.toString();
    }

    /**
     * Restore the state of the game from the given string.
     */
    public void putState(String gameData) {
        String[] fields = gameData.split(",");
        int index = 0;
        lastLarge = Integer.parseInt(fields[index++]);
        lastSmall = Integer.parseInt(fields[index++]);
        for (int large = 0; large < 9; large++) {
            for (int small = 0; small < 9; small++) {
                Tile.Owner owner = Tile.Owner.valueOf(fields[index++]);
                smallTiles[large][small].setOwner(owner);
            }
        }
        setAvailableFromLastMove(lastSmall);
        updateAllTiles();
    }

    private void updateAllTiles() {
        entireBoard.updateDrawableState();
        for (int large = 0; large < 9; large++) {
            largeTiles[large].updateDrawableState();
            for (int small = 0; small < 9; small++) {
                smallTiles[large][small].updateDrawableState();
            }
        }
    }

}
