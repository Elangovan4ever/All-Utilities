<Global.Microsoft.VisualBasic.CompilerServices.DesignerGenerated()> _
Partial Class Form1
    Inherits System.Windows.Forms.Form

    'Form overrides dispose to clean up the component list.
    <System.Diagnostics.DebuggerNonUserCode()> _
    Protected Overrides Sub Dispose(ByVal disposing As Boolean)
        Try
            If disposing AndAlso components IsNot Nothing Then
                components.Dispose()
            End If
        Finally
            MyBase.Dispose(disposing)
        End Try
    End Sub

    'Required by the Windows Form Designer
    Private components As System.ComponentModel.IContainer

    'NOTE: The following procedure is required by the Windows Form Designer
    'It can be modified using the Windows Form Designer.  
    'Do not modify it using the code editor.
    <System.Diagnostics.DebuggerStepThrough()> _
    Private Sub InitializeComponent()
        Me.ElaCustomGroupBox4 = New ElaCustomGroupBoxControl.ElaCustomGroupBox()
        Me.ElaCustomGroupBox3 = New ElaCustomGroupBoxControl.ElaCustomGroupBox()
        Me.ElaCustomGroupBox2 = New ElaCustomGroupBoxControl.ElaCustomGroupBox()
        Me.ElaCustomGroupBox1 = New ElaCustomGroupBoxControl.ElaCustomGroupBox()
        Me.TextBox1 = New System.Windows.Forms.TextBox()
        Me.ElaCustomGroupBox1.SuspendLayout()
        Me.SuspendLayout()
        '
        'ElaCustomGroupBox4
        '
        Me.ElaCustomGroupBox4.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomGroupBox4.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomGroupBox4.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomGroupBox4.BorderThickness = ElaCustomGroupBoxControl.ElaCustomGroupBox.BorderThicknessEnum.Medium
        Me.ElaCustomGroupBox4.Location = New System.Drawing.Point(46, 152)
        Me.ElaCustomGroupBox4.Name = "ElaCustomGroupBox4"
        Me.ElaCustomGroupBox4.Size = New System.Drawing.Size(388, 114)
        Me.ElaCustomGroupBox4.TabIndex = 3
        Me.ElaCustomGroupBox4.TabStop = False
        Me.ElaCustomGroupBox4.Text = "ElaCustomGroupBox4"
        '
        'ElaCustomGroupBox3
        '
        Me.ElaCustomGroupBox3.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomGroupBox3.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomGroupBox3.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomGroupBox3.BorderThickness = ElaCustomGroupBoxControl.ElaCustomGroupBox.BorderThicknessEnum.Thin
        Me.ElaCustomGroupBox3.Location = New System.Drawing.Point(46, 398)
        Me.ElaCustomGroupBox3.Name = "ElaCustomGroupBox3"
        Me.ElaCustomGroupBox3.Size = New System.Drawing.Size(388, 100)
        Me.ElaCustomGroupBox3.TabIndex = 2
        Me.ElaCustomGroupBox3.TabStop = False
        Me.ElaCustomGroupBox3.Text = "ElaCustomGroupBox3"
        '
        'ElaCustomGroupBox2
        '
        Me.ElaCustomGroupBox2.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomGroupBox2.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomGroupBox2.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomGroupBox2.BorderThickness = ElaCustomGroupBoxControl.ElaCustomGroupBox.BorderThicknessEnum.Normal
        Me.ElaCustomGroupBox2.Location = New System.Drawing.Point(46, 272)
        Me.ElaCustomGroupBox2.Name = "ElaCustomGroupBox2"
        Me.ElaCustomGroupBox2.Size = New System.Drawing.Size(388, 107)
        Me.ElaCustomGroupBox2.TabIndex = 1
        Me.ElaCustomGroupBox2.TabStop = False
        Me.ElaCustomGroupBox2.Text = "ElaCustomGroupBox2   "
        '
        'ElaCustomGroupBox1
        '
        Me.ElaCustomGroupBox1.BorderColor = System.Drawing.Color.DeepSkyBlue
        Me.ElaCustomGroupBox1.BorderColorFocus = System.Drawing.Color.Orange
        Me.ElaCustomGroupBox1.BorderColorMouseEnter = System.Drawing.Color.Green
        Me.ElaCustomGroupBox1.BorderThickness = ElaCustomGroupBoxControl.ElaCustomGroupBox.BorderThicknessEnum.Thick
        Me.ElaCustomGroupBox1.Controls.Add(Me.TextBox1)
        Me.ElaCustomGroupBox1.Location = New System.Drawing.Point(46, 21)
        Me.ElaCustomGroupBox1.Name = "ElaCustomGroupBox1"
        Me.ElaCustomGroupBox1.Size = New System.Drawing.Size(388, 116)
        Me.ElaCustomGroupBox1.TabIndex = 0
        Me.ElaCustomGroupBox1.TabStop = False
        Me.ElaCustomGroupBox1.Text = "ElaCustomGroupBox1"
        '
        'TextBox1
        '
        Me.TextBox1.Location = New System.Drawing.Point(75, 75)
        Me.TextBox1.Name = "TextBox1"
        Me.TextBox1.Size = New System.Drawing.Size(100, 20)
        Me.TextBox1.TabIndex = 0
        '
        'Form1
        '
        Me.AutoScaleDimensions = New System.Drawing.SizeF(6.0!, 13.0!)
        Me.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font
        Me.ClientSize = New System.Drawing.Size(992, 593)
        Me.Controls.Add(Me.ElaCustomGroupBox4)
        Me.Controls.Add(Me.ElaCustomGroupBox3)
        Me.Controls.Add(Me.ElaCustomGroupBox2)
        Me.Controls.Add(Me.ElaCustomGroupBox1)
        Me.Name = "Form1"
        Me.Text = "Form1"
        Me.ElaCustomGroupBox1.ResumeLayout(False)
        Me.ElaCustomGroupBox1.PerformLayout()
        Me.ResumeLayout(False)

    End Sub

    Friend WithEvents ElaCustomGroupBox1 As ElaCustomGroupBoxControl.ElaCustomGroupBox
    Friend WithEvents ElaCustomGroupBox2 As ElaCustomGroupBoxControl.ElaCustomGroupBox
    Friend WithEvents ElaCustomGroupBox3 As ElaCustomGroupBoxControl.ElaCustomGroupBox
    Friend WithEvents ElaCustomGroupBox4 As ElaCustomGroupBoxControl.ElaCustomGroupBox
    Friend WithEvents TextBox1 As TextBox
End Class
