// ORIGINAL: java/PageParamInfo.java

package info

import (
	"fmt"
)

// LinearFormula stores the coefficient and delta values of the linear formula:
// pageParamValue = coefficient * pageNum + delta.
type LinearFormula struct {
	Coefficient int
	Delta       int
}

func NewLinearFormula(coefficient, delta int) *LinearFormula {
	return &LinearFormula{
		Coefficient: coefficient,
		Delta:       delta,
	}
}

func (lf *LinearFormula) String() string {
	return fmt.Sprintf("coefficient=%d, delta=%d", lf.Coefficient, lf.Delta)
}
