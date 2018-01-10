# Error Level Analysis for ImageJ

## History:  

This plugin was implemented in July 2012 

## ImageJ's version:   

1.46a (used to develop this plugin)

## Installation:

Download ErrorLevelAnalysis.rar and unzip its content in the plugins folder then compile "Error_Level_Analysis.java" with the "Compile and Run" command. Restart ImageJ to add the "Error Level Analysis" command to the Plugins menu. 

## Description:

This plugin implements Error Level Analysis (ELA) in ImageJ.

ELA shows the amount of difference that occurs during a JPEG resave. More white means more change, and black indicates no change.

A real, camera-original picture, should have a lot of white, almost like noise, over the entire picture. As the picture is repeatedly resaved (not copied, but actually loaded into a program and saved again as a JPEG), high frequencies and fine details are removed. With each resave, more frequencies/details are lost until the picture cannot get any worse (returning a black ELA picture).

ELA works by intentionally resaving the image at a known error rate, such as 95%, and then computing the difference between the images. If there is virtually no change, then the cell has reached its local minima for error at that quality level. However, if there is a large amount of change, then the pixels are not at their local minima and are effectively original.

## Usage

Here is a brief description:
Open an image to analyze. Then follow these steps:

1. Click on Error Level Analysis in the Plugins menu;

2. Select the desidered quality for the image to save;

3. Select the scale level;

4. Click on the “OK” button to apply the filter to the image;

The plugin will show the corresponding Error Level Analysis image. This plugin can also save a report of the operations executed.

# Example

Original

![alt text](https://raw.githubusercontent.com/SebMilardo/ErrorLevelAnalysis/master/images/lenna.jpg)

Digital Manipulation

![alt text](https://raw.githubusercontent.com/SebMilardo/ErrorLevelAnalysis/master/images/lenna2.jpg)

ELA

![alt text](https://raw.githubusercontent.com/username/ErrorLevelAnalysis/master/images/lenna3.jpg)