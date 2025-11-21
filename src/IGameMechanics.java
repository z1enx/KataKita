interface IGameMechanics {
    void startGame();
    void submitGuess(String guess);
    int calculateScore();
}