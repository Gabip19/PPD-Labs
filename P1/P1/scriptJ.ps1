# Variables for program and arguments
$javaExecutable = "java"
$outDirectory = "out"

# Main class of your program (replace "Client" with your actual module name)
$moduleName = "Client"
$mainClass = "Main"

# Arguments received by the script (replace with your actual arguments)
$scriptArgs = $args

# Array to store individual execution times
$executionTimes = @()

# Run the program five times
for ($i = 1; $i -le 5; $i++) {
    # Construct classpath including the module directory and out directory
    $classpath = "$outDirectory/production/$moduleName;$outDirectory/production/Networking"

    # Run the Java program and capture the output
    $output = & $javaExecutable -cp $classpath $mainClass $i $args[1] $args[2] $args[3]

    # Extract the time from the output (replace with your actual extraction logic)
    $timeRegex = "\d+(\.\d+)?"
    $timeMatches = $output | Select-String -Pattern $timeRegex -AllMatches

    # Initialize an array to store extracted times
    $extractedTimes = @()

    # Iterate through matches and extract time values
    foreach ($match in $timeMatches.Matches) {
        $timeValue = $match.Value
        $extractedTimes += [double]$timeValue
    }

    # Add the extracted times to the array
    $executionTimes += $extractedTimes
}

# Calculate the average time
$averageTime = ($executionTimes | Measure-Object -Average).Average

# Output the results
Write-Host "Individual Execution Times: $executionTimes"
Write-Host "Average Time: $averageTime milliseconds"
