General Usage of DeDup
================================

Prerequisites
-------------

DeDup requires a running Java Runtime Environment 8 or later to be run on your local machine. In case you want to use it on Windows (which should be entirely possible, but no guarantees on this), you may
also run it there from a commandline. You can download the latest release from GitHub as listed under "Releases".


Execution
---------

You can execute DeDup as follows:

.. code-block:: bash

   java -jar DeDup.jar -h

to get some help shown. This should already explain how to use the program in general.

Options
^^^^^^^

.. code-block:: bash

   -h: Shows the help page
   -i: Select your input, otherwise you may use pipes to pipe in your data
   -m: The input only contains merged reads - don't care about missing prefixes for merged/reverse/forward reads
   -o: output file, typically BAM.

