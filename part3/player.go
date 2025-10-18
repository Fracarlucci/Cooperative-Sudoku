package main

import (
	"fmt"
	"math/rand"
	"sync"
)

type Player struct {
	ID          int
	Name        string
	MaxValue    int
	OracleChan  chan Message
	MessageChan chan Message
	MinPossible int
	MaxPossible int
	Running     bool
	mu          sync.Mutex
}

func NewPlayer(id int, maxValue int, oracleChan chan Message) *Player {
	return &Player{
		ID:          id,
		Name:        fmt.Sprintf("Player_%d", id),
		MaxValue:    maxValue,
		OracleChan:  oracleChan,
		MessageChan: make(chan Message, 10),
		MinPossible: 0,
		MaxPossible: maxValue,
		Running:     true,
	}
}

func (p *Player) Run(wg *sync.WaitGroup) {
	defer wg.Done()

	for p.Running {
		msg := <-p.MessageChan
		switch msg.MsgType {
		case StartRound:
			p.makeGuess()
		case Hint:
			p.processHint(msg)
		case Win:
			fmt.Printf("[%s] HO VINTO! Il numero era %d\n", p.Name, msg.Number)
			p.Running = false
		case Lose:
			fmt.Printf("[%s] Ho perso\n", p.Name)
			p.Running = false
		}
	}
}

func (p *Player) makeGuess() {

	p.mu.Lock()
	guess := rand.Intn(p.MaxPossible-p.MinPossible+1) + p.MinPossible
	p.mu.Unlock()

	fmt.Printf("[%s] Tento: %d (range: %d-%d)\n",
		p.Name, guess, p.MinPossible, p.MaxPossible)

	// Send message to the oracle
	msg := Message{
		MsgType:    Guess,
		Sender:     p.Name,
		PlayerName: p.Name,
		Guess:      guess,
		PlayerChan: p.MessageChan,
	}
	p.OracleChan <- msg
}

func (p *Player) processHint(msg Message) {
	p.mu.Lock()
	defer p.mu.Unlock()

	switch msg.Hint {
	case "higher":
		p.MinPossible = msg.Guess + 1
		fmt.Printf("[%s] Il numero è maggiore di %d\n", p.Name, msg.Guess)
	case "lower":
		p.MaxPossible = msg.Guess - 1
		fmt.Printf("[%s] Il numero è minore di %d\n", p.Name, msg.Guess)
	}
}
