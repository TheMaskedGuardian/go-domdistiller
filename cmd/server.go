/*
Copyright © 2023 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/go-shiori/dom"
	"github.com/golang-jwt/jwt"
	"github.com/omnivore-app/go-domdistiller/distiller"
	"github.com/spf13/cobra"
)

// serverCmd represents the server command
var serverCmd = &cobra.Command{
	Use:   "server",
	Short: "Starts a server that accepts HTML and returns the main content",
	Run: func(cmd *cobra.Command, args []string) {
		start()
	},
}

func start() {
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
	// decode JWT token and check if it's valid
	token, err := jwt.Parse(r.Header.Get("Authorization"), func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(os.Getenv("JWT_SECRET")), nil
	})
	if err != nil {
		w.WriteHeader(http.StatusUnauthorized)
		fmt.Fprint(w, "Unauthorized")
		return
	}
	if !token.Valid {
		w.WriteHeader(http.StatusUnauthorized)
		fmt.Fprint(w, "Unauthorized")
		return
	}

	// Parse request body
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		log.Println("Failed to read request body:", err)
		w.WriteHeader(http.StatusBadRequest)
		fmt.Fprint(w, "Failed to read request body")
		return
	}

	// Apply distiller
	result, err := distiller.ApplyForReader(strings.NewReader(string(body)), nil)
	if err != nil {
		fmt.Println("Failed to apply distiller:", err)
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Fprint(w, "Failed to apply distiller")
		return
	}

	// Print result
	rawHTML := dom.OuterHTML(result.Node)
	fmt.Fprint(w, rawHTML)
}

func init() {
	rootCmd.AddCommand(serverCmd)

	serverCmd.Flags().StringP("port", "p", "8080", "Port to listen on")
}
