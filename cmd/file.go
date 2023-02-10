/*
Copyright © 2023 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"fmt"
	"os"

	"github.com/go-shiori/dom"
	"github.com/omnivore-app/go-domdistiller/distiller"
	"github.com/spf13/cobra"
)

// fileCmd represents the file command
var fileCmd = &cobra.Command{
	Use:   "file",
	Short: "Extracts the main content from a file",
	Run: func(cmd *cobra.Command, args []string) {
		extractFromFile(cmd.Flag("input").Value.String(), cmd.Flag("output").Value.String())
	},
}

func extractFromFile(inputPath string, outputPath string) {
	// Apply distiller
	result, err := distiller.ApplyForFile(inputPath, nil)
	if err != nil {
		panic(err)
	}

	// Print result
	rawHTML := dom.OuterHTML(result.Node)

	file, err := os.Create(outputPath)
	if err != nil {
		panic(err)
	}
	fmt.Fprint(file, rawHTML)
}

func init() {
	rootCmd.AddCommand(fileCmd)

	fileCmd.Flags().StringP("input", "i", "", "Path to the file to extract the main content from")
	fileCmd.MarkFlagRequired("input")
	fileCmd.Flags().StringP("output", "o", "", "Path to the file to write the extracted content to")
	fileCmd.MarkFlagRequired("output")
}
