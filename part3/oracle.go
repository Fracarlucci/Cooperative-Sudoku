package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

type Oracle struct {
	NumPlayers   int
	MaxValue     int
	SecretNumber int
	MessageChan  chan Message
	Players      []*Player
	RoundNumber  int
	Running      bool
	mu           sync.Mutex
}

func NewOracle(numPlayers int, maxValue int) *Oracle {
	rand.New(rand.NewSource(time.Now().UnixNano()))
	return &Oracle{
		NumPlayers:   numPlayers,
		MaxValue:     maxValue,
		SecretNumber: rand.Intn(maxValue + 1),
		MessageChan:  make(chan Message, 100),
		Players:      make([]*Player, 0),
		RoundNumber:  0,
		Running:      true,
	}
}

func (o *Oracle) RegisterPlayer(player *Player) {
	// not thread-safe because there are more goroutines registering simultaneously
	o.mu.Lock()
	defer o.mu.Unlock()
	o.Players = append(o.Players, player)
	fmt.Printf("[ORACLE] %s registrato (%d/%d)\n",
		player.Name, len(o.Players), o.NumPlayers)
}

func (o *Oracle) Run(wg *sync.WaitGroup) {
	defer wg.Done()

	fmt.Printf("[ORACLE] Numero segreto estratto (range 0-%d)\n", o.MaxValue)

	// Wait for players
	for {
		o.mu.Lock()
		ready := len(o.Players) >= o.NumPlayers
		o.mu.Unlock()
		if ready {
			break
		}
		time.Sleep(100 * time.Millisecond)
	}

	fmt.Printf("[ORACLE] Tutti i giocatori pronti. Iniziamo!\n\n")

	for o.Running {
		o.RoundNumber++
		fmt.Printf("\n%s\n", "============================================================")
		fmt.Printf("[ORACLE] ROUND %d\n", o.RoundNumber)
		fmt.Printf("%s\n", "============================================================")

		o.startRound()

		guesses := o.collectGuesses()

		winner := o.processGuesses(guesses)

		if winner != "" {
			o.Running = false
			fmt.Printf("\n[ORACLE] Gioco terminato dopo %d round!\n", o.RoundNumber)
			break
		}
	}
}

// Send a message to players to tell them the round has started
func (o *Oracle) startRound() {
	msg := Message{
		MsgType: StartRound,
		Sender:  "Oracle",
	}
	for _, player := range o.Players {
		player.MessageChan <- msg
	}
	time.Sleep(500 * time.Millisecond)
}

// Get guesses from players
func (o *Oracle) collectGuesses() []Message {
	guesses := make([]Message, 0)

	for len(guesses) < o.NumPlayers {
		msg := <-o.MessageChan
		if msg.MsgType == Guess {
			guesses = append(guesses, msg)
		}
	}
	return guesses
}

// Iterate over guesses and check for a winner
func (o *Oracle) processGuesses(guesses []Message) string {
	for _, guess := range guesses {
		if guess.Guess == o.SecretNumber {
			fmt.Printf("\n[ORACLE] %s ha indovinato!\n", guess.PlayerName)

			// Send win message to the winner
			winMsg := Message{
				MsgType: Win,
				Sender:  "Oracle",
				Number:  o.SecretNumber,
			}
			guess.PlayerChan <- winMsg

			// Send lose message to other players
			for _, player := range o.Players {
				if player.Name != guess.PlayerName {
					loseMsg := Message{
						MsgType: Lose,
						Sender:  "Oracle",
						Winner:  guess.PlayerName,
						Number:  o.SecretNumber,
					}
					player.MessageChan <- loseMsg
				}
			}

			return guess.PlayerName
		} else {
			// Send hints to players
			hint := "higher"
			if guess.Guess > o.SecretNumber {
				hint = "lower"
			}
			hintMsg := Message{
				MsgType: Hint,
				Sender:  "Oracle",
				Guess:   guess.Guess,
				Hint:    hint,
			}
			guess.PlayerChan <- hintMsg
		}
	}

	return ""
}
