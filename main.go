package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/go-shiori/dom"
	"github.com/omnivore-app/go-domdistiller/distiller"
)

func main() {
	log.Print("starting server...")
	http.HandleFunc("/", handler)

	// Determine port for HTTP service.
	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
		log.Printf("defaulting to port %s", port)
	}

	// Start HTTP server.
	log.Printf("listening on port %s", port)
	if err := http.ListenAndServe(":"+port, nil); err != nil {
		log.Fatal(err)
	}
}

func handler(w http.ResponseWriter, r *http.Request) {
	// Parse request body
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		fmt.Println("Failed to read request body:", err)
		return
	}

	// Apply distiller
	result, err := distiller.ApplyForReader(strings.NewReader(string(body)), nil)
	if err != nil {
		fmt.Println("Failed to apply distiller:", err)
		return
	}

	// Print result
	rawHTML := dom.OuterHTML(result.Node)
	fmt.Fprint(w, rawHTML)
}
