package main

import (
	"fmt"
	"os"
	"strconv"
	"sync"
)

type MessageType int

const (
	NUM_PLAYERS = 4
)

// Message types
const (
	StartRound MessageType = iota
	Guess
	Hint
	Win
	Lose
)

type Message struct {
	MsgType    MessageType
	Sender     string
	PlayerName string
	Guess      int
	Hint       string
	Number     int
	Winner     string
	PlayerChan chan Message
}

func main() {

	args := os.Args[1:]
	max_value, err := strconv.Atoi(args[0])
	if err != nil {
		fmt.Println("Errore nella conversione:", err)
		return
	}

	fmt.Printf("  - Numero giocatori: %d\n", NUM_PLAYERS)
	fmt.Printf("  - Range numeri: 0-%d\n\n", max_value)

	oracle := NewOracle(NUM_PLAYERS, max_value)

	// To sincronize goroutines
	var wg sync.WaitGroup

	players := make([]*Player, NUM_PLAYERS)
	for i := range NUM_PLAYERS {
		player := NewPlayer(i+1, max_value, oracle.MessageChan)
		players[i] = player
		oracle.RegisterPlayer(player)
	}

	wg.Add(1)
	go oracle.Run(&wg)

	for _, player := range players {
		wg.Add(1)
		go player.Run(&wg)
	}

	// Wait for all goroutines to finish
	wg.Wait()

	fmt.Println("Gioco terminato!")
}
