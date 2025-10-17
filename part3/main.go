package main

import (
	"fmt"
	"math/rand"
	"os"
	"strconv"
	"sync"
	"time"
)

// MessageType rappresenta il tipo di messaggio
type MessageType int

const (
	StartRound MessageType = iota
	Guess
	Hint
	Win
	Lose
)

// Message rappresenta un messaggio tra agenti
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

// Player rappresenta un agente giocatore
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

// NewPlayer crea un nuovo giocatore
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

// Run esegue il ciclo principale del giocatore
func (p *Player) Run(wg *sync.WaitGroup) {
	defer wg.Done()
	fmt.Printf("[%s] Inizializzato\n", p.Name)

	for p.Running {
		select {
		case msg := <-p.MessageChan:
			switch msg.MsgType {
			case StartRound:
				p.makeGuess()
			case Hint:
				p.processHint(msg)
			case Win:
				fmt.Printf("[%s] HO VINTO! Il numero era %d\n", p.Name, msg.Number)
				p.Running = false
			case Lose:
				fmt.Printf("[%s] Ho perso. %s ha indovinato il numero %d\n",
					p.Name, msg.Winner, msg.Number)
				p.Running = false
			}
		case <-time.After(100 * time.Millisecond):
			// Timeout per verificare se continuare
		}
	}
}

// makeGuess genera un tentativo basato sulla strategia
func (p *Player) makeGuess() {
	// Piccolo delay casuale per simulare concorrenza
	time.Sleep(time.Duration(10+rand.Intn(40)) * time.Millisecond)

	p.mu.Lock()
	// Strategia: binary search - prova il valore medio
	guess := rand.Intn(p.MaxPossible-p.MinPossible+1) + p.MinPossible
	p.mu.Unlock()

	fmt.Printf("[%s] Tento: %d (range: %d-%d)\n",
		p.Name, guess, p.MinPossible, p.MaxPossible)

	// Invia il tentativo all'oracolo
	msg := Message{
		MsgType:    Guess,
		Sender:     p.Name,
		PlayerName: p.Name,
		Guess:      guess,
		PlayerChan: p.MessageChan,
	}
	p.OracleChan <- msg
}

// processHint aggiorna la strategia basandosi sul suggerimento
func (p *Player) processHint(msg Message) {
	p.mu.Lock()
	defer p.mu.Unlock()

	if msg.Hint == "higher" {
		p.MinPossible = msg.Guess + 1
		fmt.Printf("[%s] Il numero è maggiore di %d\n", p.Name, msg.Guess)
	} else if msg.Hint == "lower" {
		p.MaxPossible = msg.Guess - 1
		fmt.Printf("[%s] Il numero è minore di %d\n", p.Name, msg.Guess)
	}
}

// Oracle rappresenta l'agente oracolo che gestisce il gioco
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

// NewOracle crea un nuovo oracolo
func NewOracle(numPlayers int, maxValue int) *Oracle {
	rand.Seed(time.Now().UnixNano())
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

// RegisterPlayer registra un nuovo giocatore
func (o *Oracle) RegisterPlayer(player *Player) {
	o.mu.Lock()
	defer o.mu.Unlock()
	o.Players = append(o.Players, player)
	fmt.Printf("[ORACLE] %s registrato (%d/%d)\n",
		player.Name, len(o.Players), o.NumPlayers)
}

// Run esegue il ciclo principale dell'oracolo
func (o *Oracle) Run(wg *sync.WaitGroup) {
	defer wg.Done()

	fmt.Printf("[ORACLE] Numero segreto estratto (range 0-%d)\n", o.MaxValue)
	fmt.Printf("[ORACLE] In attesa di %d giocatori...\n", o.NumPlayers)

	// Attende che tutti i giocatori siano pronti
	for {
		o.mu.Lock()
		ready := len(o.Players) >= o.NumPlayers
		o.mu.Unlock()
		if ready {
			break
		}
		time.Sleep(100 * time.Millisecond)
	}

	fmt.Printf("[ORACLE] Tutti i giocatori pronti. Inizio il gioco!\n\n")
	time.Sleep(500 * time.Millisecond)

	// Loop principale del gioco
	for o.Running {
		o.RoundNumber++
		fmt.Printf("\n%s\n", "============================================================")
		fmt.Printf("[ORACLE] ROUND %d\n", o.RoundNumber)
		fmt.Printf("%s\n", "============================================================")

		// Segnala a tutti i giocatori di inviare il tentativo
		o.startRound()

		// Raccoglie i tentativi in ordine di arrivo
		guesses := o.collectGuesses()

		// Processa i tentativi
		winner := o.processGuesses(guesses)

		if winner != "" {
			o.Running = false
			fmt.Printf("\n[ORACLE] Gioco terminato dopo %d round!\n", o.RoundNumber)
			break
		}

		time.Sleep(500 * time.Millisecond)
	}
}

// startRound segnala l'inizio di un nuovo round a tutti i giocatori
func (o *Oracle) startRound() {
	msg := Message{
		MsgType: StartRound,
		Sender:  "Oracle",
	}
	for _, player := range o.Players {
		player.MessageChan <- msg
	}
}

// collectGuesses raccoglie i tentativi dai giocatori in ordine di arrivo
func (o *Oracle) collectGuesses() []Message {
	guesses := make([]Message, 0)
	timeout := time.After(2 * time.Second)

	for len(guesses) < o.NumPlayers {
		select {
		case msg := <-o.MessageChan:
			if msg.MsgType == Guess {
				guesses = append(guesses, msg)
			}
		case <-timeout:
			fmt.Printf("[ORACLE] Timeout: raccolti solo %d/%d tentativi\n",
				len(guesses), o.NumPlayers)
			return guesses
		}
	}

	return guesses
}

// processGuesses processa i tentativi e determina se c'è un vincitore
func (o *Oracle) processGuesses(guesses []Message) string {
	for _, guess := range guesses {
		if guess.Guess == o.SecretNumber {
			// Vincitore trovato!
			fmt.Printf("\n[ORACLE] ✓ %s ha indovinato!\n", guess.PlayerName)

			// Invia messaggio di vittoria
			winMsg := Message{
				MsgType: Win,
				Sender:  "Oracle",
				Number:  o.SecretNumber,
			}
			guess.PlayerChan <- winMsg

			// Invia messaggio di sconfitta agli altri
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
			// Invia suggerimento
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

func main() {
	fmt.Println("============================================================")
	fmt.Println("               GUESS THE NUMBER GAME")
	fmt.Println("============================================================")

	// Parametri del gioco
	const (
		NUM_PLAYERS = 4
	)

	args := os.Args[1:]
	max_value, err := strconv.Atoi(args[0])
	if err != nil {
		fmt.Println("Errore nella conversione:", err)
		return
	}

	fmt.Printf("Configurazione:\n")
	fmt.Printf("  - Numero giocatori: %d\n", NUM_PLAYERS)
	fmt.Printf("  - Range numeri: 0-%d\n\n", max_value)

	// Crea l'oracolo
	oracle := NewOracle(NUM_PLAYERS, max_value)

	// WaitGroup per sincronizzare tutti i goroutine
	var wg sync.WaitGroup

	// Crea i giocatori
	players := make([]*Player, NUM_PLAYERS)
	for i := 0; i < NUM_PLAYERS; i++ {
		player := NewPlayer(i+1, max_value, oracle.MessageChan)
		players[i] = player
		oracle.RegisterPlayer(player)
	}

	// Avvia l'oracolo
	wg.Add(1)
	go oracle.Run(&wg)

	// Avvia tutti i giocatori
	for _, player := range players {
		wg.Add(1)
		go player.Run(&wg)
	}

	// Attende il completamento di tutti i goroutine
	wg.Wait()

	fmt.Println("\n============================================================")
	fmt.Println("Gioco terminato!")
	fmt.Println("============================================================")
}
