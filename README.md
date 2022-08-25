# The Effect of the LID on Diversity Search

**This repository contains the source code to reproduce the implementation reported in the manuscript _Pushing diversity into higher dimensions: The LID effect on diversified similarity searching_, submitted to the Information Systems Journal.**

## Repository Structure

The repository tree is structured as follows.

| Directory | Description |
| ------ | ------ |
| [root](https://github.com/Jasbick/diversity_browsing)  | This folder. |
| [br/uff/LabESI/SimilaritySearch/examples](https://github.com/Jasbick/LIDEffectOnDivSearch/tree/main/br/uff/LabESI/SimilaritySearch/examples) | Contains main routines of _diversity browsing_ and BRID. |
| [br/uff/LabESI/SimilaritySearch/algorithms](https://github.com/Jasbick/LIDEffectOnDivSearch/tree/main/br/uff/LabESI/SimilaritySearch/algorithms) | Contains the implementations of _diversity browsing_ and BRID. |
| [examples](https://github.com/Jasbick/LIDEffectOnDivSearch/tree/main/examples) | A running example is provided as a shellscript [run.sh](https://github.com/Jasbick/LIDEffectOnDivSearch/tree/main/examples/run.sh). |

## Experiments

The implementations of both _distance_ and _diversity browsing_ algorithms can be found inside the _br\_uff\_LabESI\_SimilaritySearch_ folder. The code is implemented in Java version 1.8.0_301 with the Java Development Kit (JDK) version 17.

The experiments were executed in our local cluster, a QLustar server with two nodes, each with 48 AMD Opteron 2.2GHz hyper-thread cores, 96GB of RAM, a 1 TB SATA hard drive and a dedicated JVM process reserved for the experiments.   

A step-by-step guide to execute both _diversity browsing_ and a sequential scan BRID implementation can be found in the _examples_ folder. A more detailed explanation on how to execute these examples can be found in the README.md inside the _examples_ folder.

## Directory Structure

The directory tree is structured as follows.

| Dir | Description |
| ------ | ------ |
| [root](https://github.com/Jasbick/diversity_browsing)  | This folder. |
| [br/uff/LabESI/SimilaritySearch](https://github.com/Jasbick/LIDEffectOnDivSearch/tree/main/br/uff/LabESI/SimilaritySearch) | Path to the Java implementation. |
| [examples](https://github.com/Jasbick/LIDEffectOnDivSearch/tree/main/examples) | Step-by-step guide to execute Diversity Browsing. |

## Notes

_(C) THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OF THIS SOFTWARE OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE._
